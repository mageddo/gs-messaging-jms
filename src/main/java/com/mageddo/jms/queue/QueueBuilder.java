package com.mageddo.jms.queue;

import org.apache.activemq.command.ActiveMQDestination;

import static com.mageddo.jms.utils.PropertiesUtils.prop;
import static com.mageddo.jms.utils.QueueUtils.queue;

/**
 * Created by elvis on 21/05/17.
 */
public class QueueBuilder {

	public static ActiveMQDestination pingQueue() {

		return queue(DestinationConstants.PING,
			prop()
				.put("consumer.dispatchAsync", false)
		);
	}
}
