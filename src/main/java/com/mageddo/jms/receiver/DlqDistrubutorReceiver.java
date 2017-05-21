package com.mageddo.jms.receiver;

import com.mageddo.jms.queue.QueueConstants;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by elvis on 13/05/17.
 */
@Component
public class DlqDistrubutorReceiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(DlqDistrubutorReceiver.class);

	@Autowired
	private JmsTemplate jmsTemplate;

	@JmsListener(destination = QueueConstants.DEFAULT_DLQ, containerFactory = QueueConstants.DEFAULT_DLQ + "Factory")
	@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
	public void consume(Message message) throws JMSException {

		try {
			final String deliveryFailureCause = message.getStringProperty("dlqDeliveryFailureCause");
			final Matcher matcher = Pattern.compile(".*destination = queue://([^,]+),.*").matcher(deliveryFailureCause);
			final ActiveMQQueue dlqQueue;
			if (matcher.find()) {
				dlqQueue = new ActiveMQQueue(matcher.group(1));
			} else {
				dlqQueue = new ActiveMQQueue("dlq.general");
			}
			dlqQueue.setDLQ();
			jmsTemplate.convertAndSend(dlqQueue, message);

		} catch (Throwable e) {
			LOGGER.error("msg={}", e.getMessage(), e);
			throw e;
		}

	}

}
