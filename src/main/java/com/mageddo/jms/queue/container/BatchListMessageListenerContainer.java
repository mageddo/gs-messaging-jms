package com.mageddo.jms.queue.container;

import com.mageddo.jms.queue.DestinationEnum;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.connection.JmsResourceHolder;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.JmsUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by elvis on 22/05/17.
 */
public class BatchListMessageListenerContainer extends DefaultMessageListenerContainer {

	/**
	 * Qtd of redeliveries tries
	 */
	public static final String DELIVERIES = "deliveries";
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private MessageListenerContainerResourceFactory transactionalResourceFactory = new MessageListenerContainerResourceFactory();
	private int batchSize;
	private final RedeliveryPolicy redeliveryPolicy;
	private BatchMessageListener messageListener;
	private String subscriptionName;

	public BatchListMessageListenerContainer(int batchSize, RedeliveryPolicy redeliveryPolicy) {
		this.batchSize = batchSize;
		this.redeliveryPolicy = redeliveryPolicy;
	}

	@Override
	protected boolean doReceiveAndExecute(Object invoker, Session session, MessageConsumer consumer, TransactionStatus status) throws JMSException {
		Connection conToClose = null;
		Session sessionToClose = null;
		MessageConsumer consumerToClose = null;
		try {
			Session sessionToUse = session;
			boolean transactional = false;
			if (sessionToUse == null) {
				sessionToUse = ConnectionFactoryUtils.doGetTransactionalSession(
					getConnectionFactory(), this.transactionalResourceFactory, true);
				transactional = (sessionToUse != null);
			}
			if (sessionToUse == null) {
				Connection conToUse;
				if (sharedConnectionEnabled()) {
					conToUse = getSharedConnection();
				} else {
					conToUse = createConnection();
					conToClose = conToUse;
					conToUse.start();
				}
				sessionToUse = createSession(conToUse);
				sessionToClose = sessionToUse;
			}
			MessageConsumer consumerToUse = consumer;
			if (consumerToUse == null) {
				consumerToUse = createListenerConsumer(sessionToUse);
				consumerToClose = consumerToUse;
			}

			boolean exposeResource = (!transactional && isExposeListenerSession() &&
				!TransactionSynchronizationManager.hasResource(getConnectionFactory()));

			final List<ActiveMQTextMessage> msgs = new ArrayList<>();
			for (int i = 0; i < batchSize; i++) {

				final Message message = receiveMessage(consumerToUse);
				if (message != null) {
					if (logger.isTraceEnabled()) {
						logger.trace("status=Received message, msgs=" + msgs.size() + ", type=" + message.getClass() + ", consumer=" +
							consumerToUse + ", transactional=" + (transactional ? "transactional " : "") + ", session=" +
							sessionToUse);
					}
					messageReceived(invoker, sessionToUse);
					msgs.add((ActiveMQTextMessage) message);
				} else {
					if (logger.isTraceEnabled()) {
						logger.trace("consumer=" + consumerToUse + ", transactional=" + (transactional ? "transactional " : "") +
							"session=" + sessionToUse + ", status=did not receive a message");
					}
					if (!msgs.isEmpty()) {
						break;
					}
					noMessageReceived(invoker, sessionToUse);
					// Nevertheless call commit, in order to reset the transaction timeout (if any).
					if (shouldCommitAfterNoMessageReceived(sessionToUse)) {
						commitIfNecessary(sessionToUse, message);
					}
					// Indicate that no message has been received.
					return false;
				}
			}
			try {
				if (!isAcceptMessagesWhileStopping() && !isRunning()) {
					if (logger.isWarnEnabled()) {
						logger.warn("Rejecting received message because of the listener container " +
							"having been stopped in the meantime: ");
					}
					rollbackIfNecessary(sessionToUse);
					throw new RuntimeException();
				}

				try {

					if (exposeResource) {
						TransactionSynchronizationManager.bindResource(
							getConnectionFactory(), new LocallyExposedJmsResourceHolder(sessionToUse));
					}

					// consuming and get not consumed messages
					final List<ActiveMQTextMessage> notConsumedMsgs = getMessageListener().onMessage(msgs);
					if(!notConsumedMsgs.isEmpty()){

						final ActiveMQDestination destination = (ActiveMQDestination) getDestination();
						Assert.notNull(destination, "Destination can not be null");
						final DestinationEnum destinationEnum = DestinationEnum.fromDestinationName(destination.getPhysicalName());
						final MessageProducer queueProducer = sessionToUse.createProducer(destinationEnum.getDestination()),
																	dlqProducer = sessionToUse.createProducer(destinationEnum.getDlq());

						for (final ActiveMQTextMessage notConsumedMsg : notConsumedMsgs) {
							notConsumedMsg.setReadOnlyProperties(false);

							final long deliveries = getDeliveries(notConsumedMsg);
							if(deliveries < redeliveryPolicy.getMaximumRedeliveries()){

								// removing schedule to can be scheduled again
								notConsumedMsg.removeProperty("scheduledJobId");

								// redelivery policy
								notConsumedMsg.setLongProperty(DELIVERIES, deliveries + 1);
								notConsumedMsg.setLongProperty(
									ScheduledMessage.AMQ_SCHEDULED_DELAY,
									redeliveryPolicy.getNextRedeliveryDelay(redeliveryPolicy.getRedeliveryDelay())
								);

								notConsumedMsg.setReadOnlyProperties(true);
								queueProducer.send(notConsumedMsg);

							}else{
								notConsumedMsg.removeProperty(DELIVERIES);
								notConsumedMsg.setReadOnlyProperties(true);
								dlqProducer.send(notConsumedMsg);
								logger.info(
									"status=send-to-dlq, dlq={}, msgId={}", destinationEnum.getDlq().getPhysicalName(),
									notConsumedMsg.getJMSMessageID()
								);
							}

						}
						dlqProducer.close();
						queueProducer.close();
					}
				} catch (RuntimeException ex) {
					rollbackOnExceptionIfNecessary(sessionToUse, ex);
					throw ex;
				} catch (Error err) {
					rollbackOnExceptionIfNecessary(sessionToUse, err);
					throw err;
				}
				commitIfNecessary(sessionToUse, null);
			} catch (Throwable ex) {
				if (status != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("status=rolling back transaction, cause=listener exception thrown: " + ex);
					}
					status.setRollbackOnly();
				}
				handleListenerException(ex);
				// Rethrow JMSException to indicate an infrastructure problem
				// that may have to trigger recovery...
				if (ex instanceof JMSException) {
					throw (JMSException) ex;
				}
			} finally {
				if (exposeResource) {
					TransactionSynchronizationManager.unbindResource(getConnectionFactory());
				}
			}
			// Indicate that a message has been received.
			if(logger.isTraceEnabled()){
				logger.trace("status=consume-completed, msg={}", msgs.size());
			}
			return true;

		} finally {
			JmsUtils.closeMessageConsumer(consumerToClose);
			JmsUtils.closeSession(sessionToClose);
			ConnectionFactoryUtils.releaseConnection(conToClose, getConnectionFactory(), true);
		}
	}

	private long getDeliveries(ActiveMQTextMessage textMessage) throws JMSException {
		final String deliveries = textMessage.getStringProperty("deliveries");
		return StringUtils.isBlank(deliveries) ? 0 : Long.parseLong(deliveries);
	}

	@Override
	public BatchMessageListener getMessageListener() {
		return this.messageListener;
	}

	@Override
	public void setMessageListener(Object messageListener) {
		if (!(messageListener instanceof BatchMessageListener)){
			throw new IllegalArgumentException();
		}
		this.messageListener = (BatchMessageListener) messageListener;
		if (this.subscriptionName == null) {
			this.subscriptionName = getDefaultSubscriptionName(messageListener);
		}
	}

	@Override
	public String getSubscriptionName() {
		return this.subscriptionName;
	}

	@Override
	public void setSubscriptionName(String subscriptionName) {
		this.subscriptionName = subscriptionName;
	}

	/**
	 * ResourceFactory implementation that delegates to this listener container's protected callback methods.
	 */
	private class MessageListenerContainerResourceFactory implements ConnectionFactoryUtils.ResourceFactory {

		@Override
		public Connection getConnection(JmsResourceHolder holder) {
			return BatchListMessageListenerContainer.this.getConnection(holder);
		}

		@Override
		public Session getSession(JmsResourceHolder holder) {
			return BatchListMessageListenerContainer.this.getSession(holder);
		}

		@Override
		public Connection createConnection() throws JMSException {
			if (BatchListMessageListenerContainer.this.sharedConnectionEnabled()) {
				Connection sharedCon = BatchListMessageListenerContainer.this.getSharedConnection();
				return new SingleConnectionFactory(sharedCon).createConnection();
			} else {
				return BatchListMessageListenerContainer.this.createConnection();
			}
		}

		@Override
		public Session createSession(Connection con) throws JMSException {
			return BatchListMessageListenerContainer.this.createSession(con);
		}

		@Override
		public boolean isSynchedLocalTransactionAllowed() {
			return BatchListMessageListenerContainer.this.isSessionTransacted();
		}
	}


	/**
	 * This implementation checks whether the Session is externally synchronized.
	 * In this case, the Session is not locally transacted, despite the listener
	 * container's "sessionTransacted" flag being set to "true".
	 * @see org.springframework.jms.connection.JmsResourceHolder
	 */
	@Override
	protected boolean isSessionLocallyTransacted(Session session) {
		if (!super.isSessionTransacted()) {
			return false;
		}
		JmsResourceHolder resourceHolder =
			(JmsResourceHolder) TransactionSynchronizationManager.getResource(getConnectionFactory());
		return (resourceHolder == null || resourceHolder instanceof LocallyExposedJmsResourceHolder ||
			!resourceHolder.containsSession(session));
	}

	class LocallyExposedJmsResourceHolder extends JmsResourceHolder {

		public LocallyExposedJmsResourceHolder(Session session) {
			super(session);
		}
	}
}
