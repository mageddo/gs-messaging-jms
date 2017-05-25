package com.mageddo.jms.queue.container;

import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.JMSException;
import java.util.List;

/**
 * Created by elvis on 23/05/17.
 */
public interface BatchMessageListener {

	List<ActiveMQTextMessage> onMessage(List<ActiveMQTextMessage> messages) throws JMSException;

}
