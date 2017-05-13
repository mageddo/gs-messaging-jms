package com.mageddo.jms.queue;

import org.apache.activemq.command.ActiveMQQueue;

/**
 * Created by elvis on 13/05/17.
 */
public class CompleteQueue extends ActiveMQQueue implements Queue {

	private String name, factory;
	private int ttl, retries, consumers, maxConsumers;

	public CompleteQueue(String name, String factory, int ttl, int retries, int consumers, int maxConsumers) {
		super(name);
		if (factory == null){
			this.factory = name;
		}else{
			this.factory = factory;
		}
		this.name = name;
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
}
