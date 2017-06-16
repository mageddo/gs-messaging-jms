package com.mageddo.jms.queue;

import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;

/**
 * Created by elvis on 13/05/17.
 */
public class CompleteDestination implements MGDestination {

	private final ActiveMQDestination destination;
	private ActiveMQQueue dlq;
	private String name, factory;
	private int ttl;
	private int retries;
	private int consumers;
	private int maxConsumers;
	private boolean nonBlockingRedelivery;
	private boolean asyncSend;
	private boolean persistentDelivery;

	public CompleteDestination(ActiveMQDestination destination, int ttl, int retries, int consumers, int maxConsumers) {
		this(destination, ttl, retries, consumers, maxConsumers, false);
	}

	public CompleteDestination(ActiveMQDestination destination, int ttl, int retries, int consumers, int maxConsumers,
													 boolean nonBlockingRedelivery) {
		this(destination, ttl, retries, consumers, maxConsumers, nonBlockingRedelivery, true);
	}

	public CompleteDestination(ActiveMQDestination destination, int ttl, int retries, int consumers, int maxConsumers,
													 boolean nonBlockingRedelivery, boolean asyncSend) {
		this(destination, ttl, retries, consumers, maxConsumers, nonBlockingRedelivery, asyncSend, true);
	}

	public CompleteDestination(ActiveMQDestination destination, int ttl, int retries, int consumers, int maxConsumers,
														 boolean nonBlockingRedelivery, boolean asyncSend, boolean persistentDelivery) {

		this.destination = destination;
		this.name = destination.getPhysicalName();
		this.ttl = ttl;
		this.retries = retries;
		this.consumers = consumers;
		this.maxConsumers = maxConsumers;
		this.nonBlockingRedelivery = nonBlockingRedelivery;
		this.asyncSend = asyncSend;
		this.persistentDelivery = persistentDelivery;

		setFactory(destination.getPhysicalName());
		setDLQ(new ActiveMQQueue("DLQ." + getName()));
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getTTL() {
		return this.ttl;
	}

	@Override
	public int getRetries() {
		return this.retries;
	}

	@Override
	public int getConsumers() {
		return this.consumers;
	}

	@Override
	public int getMaxConsumers() {
		return this.maxConsumers;
	}

	public String getFactory() {
		return factory;
	}

	public ActiveMQDestination getDestination() {
		return destination;
	}

	public ActiveMQQueue getDLQ() {
		return dlq;
	}

	public boolean isNonBlockingRedelivery() {
		return nonBlockingRedelivery;
	}

	protected void setNonBlockingRedelivery(boolean nonBlockingRedelivery) {
		this.nonBlockingRedelivery = nonBlockingRedelivery;
	}

	protected void setFactory(String factory) {
		this.factory = factory;
	}

	protected void setDLQ(ActiveMQQueue dlq) {
		this.dlq = dlq;
	}

	public boolean isAsyncSend() {
		return asyncSend;
	}

	public void setAsyncSend(boolean asyncSend) {
		this.asyncSend = asyncSend;
	}

	public boolean isPersistentDelivery() {
		return persistentDelivery;
	}
}
