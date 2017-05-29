package com.mageddo.jms.receiver;

import com.mageddo.jms.queue.DestinationConstants;
import com.mageddo.jms.queue.DestinationEnum;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by elvis on 13/05/17.
 */
@Component
public class DlqDistributorReceiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(DlqDistributorReceiver.class);

	@Autowired
	private JmsTemplate jmsTemplate;

	@JmsListener(destination = DestinationConstants.DEFAULT_DLQ, containerFactory = DestinationConstants.DEFAULT_DLQ + "Factory")
	@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
	public void consume(ActiveMQMessage message) throws Exception {

		try {
			final ActiveMQQueue dlqQueue = getDLQ(message);
			LOGGER.debug("status=movingDLQ, dlq={}, msgId={}, cause={}", dlqQueue.getPhysicalName(),
				message.getJMSMessageID(), message.getProperty("dlqDeliveryFailureCause"));
			jmsTemplate.convertAndSend(dlqQueue, message);
		} catch (Throwable e) {
			LOGGER.error("errorMsg={}, msg={}", e.getMessage(), message.getJMSMessageID(), e);
			throw e;
		}

	}

	private ActiveMQQueue getDLQ(ActiveMQMessage message) throws JMSException {
		final DestinationEnum dlq = DestinationEnum.fromDestinationName(message.getOriginalDestination().getPhysicalName());
		if(dlq != null){
			return dlq.getDlq();
		}
		return getDLQByFailureCause(message);
	}

	private ActiveMQQueue getDLQByFailureCause(ActiveMQMessage message) throws JMSException {
		final String deliveryFailureCause = message.getStringProperty("dlqDeliveryFailureCause");
		final Matcher matcher = Pattern.compile(".*destination = queue://([^,]+),.*").matcher(deliveryFailureCause);
		final ActiveMQQueue dlqQueue;
		if (matcher.find()) {
			dlqQueue = new ActiveMQQueue(matcher.group(1));
		} else {
			dlqQueue = new ActiveMQQueue("dlq.general");
		}
		dlqQueue.setDLQ();
		return dlqQueue;
	}

}
