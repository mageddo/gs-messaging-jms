package com.mageddo.jms.queue;

import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.mageddo.jms.queue.DestinationBuilder.getInstance;

public enum DestinationEnum {

	MAIL(getInstance().mailQueue(), true),
	COLOR_TOPIC(getInstance().colorTopic(), true),
	COLOR(getInstance().colorQueue(), true),
	RED_COLOR(getInstance().redColorQueue(), true),
	PING(getInstance().pingQueue(), false),
	SALE(getInstance().saleQueue(), false),
	WITHDRAW(getInstance().withdrawQueue(), false),

	DEFAULT_DLQ(getInstance().defaultDLQ(), true)

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

	private CompleteDestination destination;
	private boolean autoDeclare;

	DestinationEnum(CompleteDestination destination, boolean autoDeclare) {
		this.destination = destination;
		this.autoDeclare = autoDeclare;
	}

	public ActiveMQQueue getDlq() {
		return this.destination.getDLQ();
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