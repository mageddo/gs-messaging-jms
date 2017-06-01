package com.mageddo.jms.queue;

import org.apache.activemq.RedeliveryPolicy;

import static com.mageddo.jms.utils.QueueUtils.queue;
import static com.mageddo.jms.utils.QueueUtils.topic;

/**
 * Created by elvis on 21/05/17.
 */
public final class DestinationBuilder {

	private static final DestinationBuilder instance = new DestinationBuilder();

	public static DestinationBuilder getInstance(){
		return instance;
	}

	public CompleteDestination mailQueue(){

		final CompleteDestination dest = new CompleteDestination(
			queue(DestinationConstants.MAIL), 60000, 3, 2, 2
		);
		return dest;

	}

	public CompleteDestination colorTopic(){

		final CompleteDestination dest = new CompleteDestination(
			topic("VirtualTopic.color"), 10000, 2, 1, 2
		);
		return dest;

	}

	public CompleteDestination colorQueue(){

		final CompleteDestination dest = new CompleteDestination(
			queue(DestinationConstants.COLOR), 10000, 2, 1, 2
		);
		return dest;

	}


	public CompleteDestination redColorQueue(){

		final CompleteDestination dest = new CompleteDestination(
			queue(DestinationConstants.COLOR), 10000, 2, 1, 2
		);
		dest.setFactory(DestinationConstants.FACTORY_RED_COLOR);
		return dest;

	}

	public CompleteDestination pingQueue() {

		final CompleteDestination dest = new CompleteDestination(
			queue(DestinationConstants.PING + "?consumer.dispatchAsync=false"),
			10000, 2, 1, 2
		);
		dest.setFactory(DestinationConstants.FACTORY_PING);
		return dest;

	}

	public CompleteDestination saleQueue() {

		final CompleteDestination dest = new CompleteDestination(
			queue(DestinationConstants.SALE),
			20000, 3, 1, 1
		);
		return dest;

	}

	public CompleteDestination withdrawQueue() {

		final CompleteDestination dest = new CompleteDestination(
			queue(DestinationConstants.WITHDRAW),
			60000, 3, 1, 1
		);
		return dest;

	}

	public CompleteDestination defaultDLQ() {

		final CompleteDestination dest = new CompleteDestination(
			queue(DestinationConstants.DEFAULT_DLQ),
			10000, RedeliveryPolicy.NO_MAXIMUM_REDELIVERIES, 1, 2
		);
		return dest;

	}
}
