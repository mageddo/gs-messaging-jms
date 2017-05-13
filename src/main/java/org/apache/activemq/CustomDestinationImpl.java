package org.apache.activemq;

import org.apache.activemq.*;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ConsumerId;

import javax.jms.*;

/**
 * Created by elvis on 13/05/17.
 */
public class CustomDestinationImpl implements CustomDestination {

	private ActiveMQDestination destination = new ActiveMQQueue("mailbox");

	@Override
	public MessageConsumer createConsumer(ActiveMQSession session, String messageSelector) {
		return createConsumer(session, messageSelector, false);
	}

	@Override
	public MessageConsumer createConsumer(ActiveMQSession session, String messageSelector, boolean noLocal) {

		final ActiveMQPrefetchPolicy prefetchPolicy = session.getConnection().getPrefetchPolicy();
		try {
			return new ActiveMQMessageConsumer(session, getConsumerId(session), destination, "x", messageSelector,
				prefetchPolicy.getQueuePrefetch(), prefetchPolicy.getMaximumPendingMessageLimit(), noLocal, false,
				session.isAsyncDispatch(), null);
		} catch (JMSException e) {
			throw new RuntimeException();
		}
	}

	private ConsumerId getConsumerId(ActiveMQSession session) {
		return session.getNextConsumerId();
	}

	@Override
	public TopicSubscriber createSubscriber(ActiveMQSession session, String messageSelector, boolean noLocal) {
		try {
			return new ActiveMQTopicSubscriber(session, getConsumerId(session), destination, null,
				messageSelector, session.getConnection().getPrefetchPolicy().getQueuePrefetch(),
				session.getConnection().getPrefetchPolicy().getMaximumPendingMessageLimit(),
				noLocal, false, session.isAsyncDispatch()) {
			};
		} catch (JMSException e) {
			throw new RuntimeException();
		}
	}

	@Override
	public TopicSubscriber createDurableSubscriber(ActiveMQSession session, String name, String messageSelector, boolean noLocal) {
		return createSubscriber(session, messageSelector, noLocal);
	}

	@Override
	public QueueReceiver createReceiver(ActiveMQSession session, String messageSelector) {
		try {
			return new ActiveMQQueueReceiver(session, getConsumerId(session),
				destination, messageSelector, session.getConnection().getPrefetchPolicy().getQueuePrefetch(),
				session.getConnection().getPrefetchPolicy().getMaximumPendingMessageLimit(),
				session.isAsyncDispatch()) {
			};
		} catch (JMSException e) {
			throw new RuntimeException();
		}
	}

	@Override
	public MessageProducer createProducer(ActiveMQSession session) throws JMSException {
		return new ActiveMQMessageProducer(session, session.getNextProducerId(), destination, 10000) {
		};
	}

	@Override
	public TopicPublisher createPublisher(ActiveMQSession session) throws JMSException {
		return new ActiveMQTopicPublisher(session, destination, 10000) {
		};
	}

	@Override
	public QueueSender createSender(ActiveMQSession session) throws JMSException {
		return new ActiveMQQueueSender(session, destination, 10000) {
		};
	}
}