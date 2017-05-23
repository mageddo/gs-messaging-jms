package com.mageddo.jms.queue.container;

import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.connection.JmsResourceHolder;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.JmsUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.jms.*;

/**
 * Created by elvis on 22/05/17.
 */
public class BatchMessageListenerContainer extends DefaultMessageListenerContainer {


	private MessageListenerContainerResourceFactory transactionalResourceFactory = new MessageListenerContainerResourceFactory();

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
				}
				else {
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
			Message message = receiveMessage(consumerToUse);
			if (message != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("status=Received message, type=" + message.getClass() + ", consumer=" +
						consumerToUse + ", transactional=" + (transactional ? "transactional " : "") + ", session=" + sessionToUse);
				}
				messageReceived(invoker, sessionToUse);
				boolean exposeResource = (!transactional && isExposeListenerSession() &&
					!TransactionSynchronizationManager.hasResource(getConnectionFactory()));
				if (exposeResource) {
					TransactionSynchronizationManager.bindResource(
						getConnectionFactory(), new JmsResourceHolder(sessionToUse));
				}
				try {
					doExecuteListener(sessionToUse, message);
				}
				catch (Throwable ex) {
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
				}
				finally {
					if (exposeResource) {
						TransactionSynchronizationManager.unbindResource(getConnectionFactory());
					}
				}
				// Indicate that a message has been received.
				return true;
			}
			else {
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
		finally {
			JmsUtils.closeMessageConsumer(consumerToClose);
			JmsUtils.closeSession(sessionToClose);
			ConnectionFactoryUtils.releaseConnection(conToClose, getConnectionFactory(), true);
		}
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
			}
			else {
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
