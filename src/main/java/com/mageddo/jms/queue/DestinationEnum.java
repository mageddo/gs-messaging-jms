package com.mageddo.jms.queue;

import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.mageddo.jms.queue.QueueBuilder.pingQueue;
import static com.mageddo.jms.utils.QueueUtils.queue;
import static com.mageddo.jms.utils.QueueUtils.topic;

public enum DestinationEnum {

	MAIL(queue(DestinationConstants.MAIL), true, 2000, 3, 5, 10),
	COLOR_TOPIC(topic("VirtualTopic.color"), true, 10000, 2, 1, 2),
	COLOR(queue(DestinationConstants.COLOR), true, 10000, 2, 1, 2),
	RED_COLOR(queue(DestinationConstants.COLOR), true, 10000, 2, 1, 2, DestinationConstants.FACTORY_RED_COLOR),
	PING(pingQueue(), false, 2000, 3, 5, 10, DestinationConstants.FACTORY_PING),
	SALE(queue(DestinationConstants.SALE), false, 20000, 3, 1, 1),
	WITHDRAW(queue(DestinationConstants.WITHDRAW), false, 60000, 3, 1, 1),

	DEFAULT_DLQ(queue(DestinationConstants.DEFAULT_DLQ), true, 10000, RedeliveryPolicy.NO_MAXIMUM_REDELIVERIES, 1, 2)

	;

	private static final Map<String, DestinationEnum> DESTINATION_BY_NAME = new HashMap<>();
	static {
		for (final DestinationEnum destinationEnum : values()) {
			final String destinationName = destinationEnum.getDestination().getPhysicalName();
			if(!DESTINATION_BY_NAME.containsKey(destinationName)){
				DESTINATION_BY_NAME.put(destinationName, destinationEnum);
			}
		}
		Collections.unmodifiableMap(DESTINATION_BY_NAME);
	}

	private ActiveMQQueue dlq;
	private CompleteDestination destination;
	private boolean autoDeclare;

	DestinationEnum(ActiveMQDestination destination, int ttl, int retries) {
		set(destination, true, ttl, retries, 1, 1, null);
	}

	DestinationEnum(ActiveMQDestination destination, boolean autoDeclare, int ttl, int retries, int consumers, int maxConsumers) {
		set(destination, autoDeclare, ttl, retries, consumers, maxConsumers, null);
	}

	DestinationEnum(ActiveMQDestination destination, boolean autoDeclare, int ttl, int retries, int consumers, int maxConsumers, String factory) {
		set(destination, autoDeclare, ttl, retries, consumers, maxConsumers, factory);
	}

	private void set(ActiveMQDestination destination, boolean autoDeclare, int ttl, int retries, int consumers, int maxConsumers, String factory) {
		this.destination = new CompleteDestination(destination, factory, ttl, retries, consumers, maxConsumers);
		this.dlq = new ActiveMQQueue("DLQ." + destination.getPhysicalName());
		this.dlq.setDLQ();
		this.autoDeclare = autoDeclare;
	}

	public ActiveMQQueue getDlq() {
		return this.dlq;
	}

	public CompleteDestination getCompleteDestination() {
		return this.destination;
	}

	public ActiveMQDestination getDestination(){
		return this.getCompleteDestination().getDestination();
	}

	public boolean isAutoDeclare() {
		return autoDeclare;
	}

	/**
	 * Obs; the destination name is not unique
	 * @param name
	 * @return
	 */
	public static DestinationEnum fromDestinationName(String name){
		return DESTINATION_BY_NAME.get(name);
	}
}