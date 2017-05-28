package com.mageddo.jms.queue.container;

import org.apache.activemq.command.ActiveMQMessage;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.*;

/**
 * Created by elvis on 28/05/17.
 */
public class BatchMessageListenerContainer extends DefaultMessageListenerContainer {

	public static final int DEFAULT_BATCH_SIZE = 100;

	protected final int batchSize = DEFAULT_BATCH_SIZE;


	/**
	 * Override the method receiveMessage to return an instance of BatchMessage - an inner class being declared further down.
	 */
	@Override
	protected Message receiveMessage(MessageConsumer consumer) throws JMSException {
		final BatchMessage batch = new BatchMessage(this);
		while (!batch.releaseAfterMessage((ActiveMQMessage) super.receiveMessage(consumer)));
		return batch.getMessages().size() == 0 ? null : batch;
	}

	@Override
	protected void executeListener(Session session, Message message) {
		final BatchMessage batchMessage = (BatchMessage) message;
		batchMessage.setSession(session);
		super.executeListener(session, message);
		batchMessage.release();
	}

	@Override
	protected void messageReceived(Object invoker, Session session) {
		super.messageReceived(invoker, session);
	}

	@Override
	protected void noMessageReceived(Object invoker, Session session) {
		super.noMessageReceived(invoker, session);
	}

}
