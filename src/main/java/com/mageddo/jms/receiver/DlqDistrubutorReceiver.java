package com.mageddo.jms.receiver;

import com.mageddo.jms.queue.QueueConstants;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by elvis on 13/05/17.
 */
@Component
public class DlqDistrubutorReceiver {

	@Autowired
	private JmsTemplate jmsTemplate;

	@JmsListener(destination = QueueConstants.DEFAULT_DLQ, containerFactory = QueueConstants.DEFAULT_DLQ + "Factory")
	public void consume(Message message) throws JMSException {
		final String deliveryFailureCause  = message.getStringProperty("dlqDeliveryFailureCause");
		final Matcher matcher = Pattern.compile(".*destination = queue://([^,]+),.*").matcher(deliveryFailureCause);
		final ActiveMQQueue dlqQueue;
		if (matcher.find()){
			dlqQueue = new ActiveMQQueue(matcher.group(1));
		}else{
			dlqQueue = new ActiveMQQueue("dlq.general");
		}
		dlqQueue.setDLQ();
		jmsTemplate.convertAndSend(dlqQueue, message);
	}
}
