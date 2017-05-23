package com.mageddo.jms.queue.container;

import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.connection.JmsResourceHolder;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.JmsUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by elvis on 22/05/17.
 */
public class BatchMessageListenerContainer extends DefaultMessageListenerContainer {

	private MessageListenerContainerResourceFactory transactionalResourceFactory = new MessageListenerContainerResourceFactory();
	private int batchSize;
	private BatchMessageListener messageListener;
	private String subscriptionName;

	public BatchMessageListenerContainer(int batchSize) {
		this.batchSize = batchSize;
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

			final List<Message> msgs = new ArrayList<>();
			for (int i = 0; i < batchSize; i++) {

				final Message message = receiveMessage(consumerToUse);
				if (message != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("status=Received message, type=" + message.getClass() + ", consumer=" +
							consumerToUse + ", transactional=" + (transactional ? "transactional " : "") + ", session=" + sessionToUse);
					}
					messageReceived(invoker, sessionToUse);
					msgs.add(message);
				} else {
					if (!msgs.isEmpty()) {
						break;
					}
					if (logger.isTraceEnabled()) {
						logger.trace("consumer=" + consumerToUse + ", transactional=" + (transactional ? "transactional " : "") +
							"session=" + sessionToUse + ", status=did not receive a message");
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
					rollbackIfNecessary(session);
					throw new RuntimeException();
				}

				try {

					if (exposeResource) {
						TransactionSynchronizationManager.bindResource(
							getConnectionFactory(), new JmsResourceHolder(sessionToUse));
					}
					getMessageListener().onMessage(msgs);
				} catch (RuntimeException ex) {
					rollbackOnExceptionIfNecessary(session, ex);
					throw ex;
				} catch (Error err) {
					rollbackOnExceptionIfNecessary(session, err);
					throw err;
				}
				commitIfNecessary(session, null);
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
			return true;

		} finally {
			JmsUtils.closeMessageConsumer(consumerToClose);
			JmsUtils.closeSession(sessionToClose);
			ConnectionFactoryUtils.releaseConnection(conToClose, getConnectionFactory(), true);
		}
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
			return BatchMessageListenerContainer.this.getConnection(holder);
		}

		@Override
		public Session getSession(JmsResourceHolder holder) {
			return BatchMessageListenerContainer.this.getSession(holder);
		}

		@Override
		public Connection createConnection() throws JMSException {
			if (BatchMessageListenerContainer.this.sharedConnectionEnabled()) {
				Connection sharedCon = BatchMessageListenerContainer.this.getSharedConnection();
				return new SingleConnectionFactory(sharedCon).createConnection();
			} else {
				return BatchMessageListenerContainer.this.createConnection();
			}
		}

		@Override
		public Session createSession(Connection con) throws JMSException {
			return BatchMessageListenerContainer.this.createSession(con);
		}

		@Override
		public boolean isSynchedLocalTransactionAllowed() {
			return BatchMessageListenerContainer.this.isSessionTransacted();
		}
	}
}
