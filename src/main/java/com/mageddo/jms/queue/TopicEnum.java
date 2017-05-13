package com.mageddo.jms.queue;

import org.apache.activemq.command.ActiveMQTopic;

/**
 * Created by elvis on 13/05/17.
 */
public enum TopicEnum {

	COLOR(new ActiveMQTopic("VirtualTopic.color"));

	private final ActiveMQTopic topic;

	TopicEnum(ActiveMQTopic topic) {
		this.topic = topic;
	}

	public ActiveMQTopic getTopic() {
		return topic;
	}
}
