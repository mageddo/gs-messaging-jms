package com.mageddo.jms.receiver;

import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by elvis on 13/05/17.
 */
@Component
public class DlqDistrubutorReceiver {

	@Autowired
	private JmsTemplate jmsTemplate;

//	@JmsListener(destination = "ActiveMQ.DLQ", concurrency = "1-2")
	public void consume(ActiveMQMessage message){
		final ActiveMQDestination jmsDestination = (ActiveMQDestination) message.getJMSDestination();
		jmsTemplate.convertAndSend("DLQ." + jmsDestination, message);
	}
}
