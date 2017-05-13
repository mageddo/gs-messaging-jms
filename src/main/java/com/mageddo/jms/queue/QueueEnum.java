package com.mageddo.jms.queue;

import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQQueue;

public enum QueueEnum {

	MAIL(QueueConstants.MAIL, 2000, 2, 1, 1),
	COLOR(QueueConstants.COLOR, 10000, 2, 1, 2),
	RED_COLOR(QueueConstants.COLOR, 10000, 2, 1, 2, QueueConstants.FACTORY_RED_COLOR),
	DEFAULT_DLQ(QueueConstants.DEFAULT_DLQ, 10000, RedeliveryPolicy.NO_MAXIMUM_REDELIVERIES, 1, 2),

	;

	private ActiveMQQueue dlq;
	private CompleteQueue queue;

	QueueEnum(String queue, int ttl, int retries) {
		set(queue, ttl, retries, 1, 1, null);
	}

	QueueEnum(String queue, int ttl, int retries, int consumers, int maxConsumers) {
		set(queue, ttl, retries, consumers, maxConsumers, null);
	}

	QueueEnum(String queue, int ttl, int retries, int consumers, int maxConsumers, String factory) {
		set(queue, ttl, retries, consumers, maxConsumers, factory);
	}

	private void set(String queue, int ttl, int retries, int consumers, int maxConsumers, String factory) {
		this.queue = new CompleteQueue(queue, factory, ttl, retries, consumers, maxConsumers);
		this.dlq = new ActiveMQQueue("dlq." + queue);
		this.dlq.setDLQ();
	}

	public ActiveMQQueue getDlq() {
		return this.dlq;
	}

	public CompleteQueue getQueue() {
		return this.queue;
	}
}