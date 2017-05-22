package com.mageddo.jms.queue;

import org.apache.activemq.command.ActiveMQDestination;

/**
 * Created by elvis on 13/05/17.
 */
public class CompleteDestination implements MGDestination {

	private final ActiveMQDestination destination;
	private String name, factory;
	private int ttl, retries, consumers, maxConsumers;

	public CompleteDestination(ActiveMQDestination destination, String factory, int ttl, int retries, int consumers, int maxConsumers) {
		this.destination = destination;
		if (factory == null){
			this.factory = destination.getPhysicalName();
		}else{
			this.factory = factory;
		}
		this.name = destination.getPhysicalName();
		this.ttl = ttl;
		this.retries = retries;
		this.consumers = consumers;
		this.maxConsumers = maxConsumers;
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
}
