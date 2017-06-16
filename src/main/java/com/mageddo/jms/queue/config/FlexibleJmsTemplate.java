package com.mageddo.jms.queue.config;

import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.jms.*;
import java.lang.IllegalStateException;
import java.lang.reflect.Method;

/**
 * Created by elvis on 15/06/17.
 *
 * This implementation supports custom TIME TO LIVE, PRIORITY, PERSISTENCE MODE
 * set by message
 */
public class FlexibleJmsTemplate extends JmsTemplate {

	private static final Method setDeliveryDelayMethod =
			ClassUtils.getMethodIfAvailable(MessageProducer.class, "setDeliveryDelay", long.class);

	public FlexibleJmsTemplate(PooledConnectionFactory connectionFactory) {
		super(connectionFactory);
	}

	@Override
	protected void doSend(MessageProducer producer, Message message) throws JMSException {

		if (this.getDeliveryDelay() >= 0) {
			if (setDeliveryDelayMethod == null) {
				throw new IllegalStateException("setDeliveryDelay requires JMS 2.0");
			}
			ReflectionUtils.invokeMethod(setDeliveryDelayMethod, producer, this.getDeliveryDelay());
		}
		if (isExplicitQosEnabled()) {
			producer.send(message, message.getJMSDeliveryMode(), message.getJMSPriority(), message.getJMSExpiration());
		} else {
			producer.send(message);
		}
	}

	@Override
	public void convertAndSend(Destination destination, final Object message) throws JmsException {
		send(destination, defaultMessageCreator(message));
	}

	@Override
	public void convertAndSend(String destinationName, final Object message) throws JmsException {
		send(destinationName, defaultMessageCreator(message));
	}

	@Override
	public void convertAndSend(
		Destination destination, final Object message, final MessagePostProcessor postProcessor)
		throws JmsException {
		send(destination, getMessageCreatorWithPostProcessor(message, postProcessor));
	}

	@Override
	public void convertAndSend(
		String destinationName, final Object message, final MessagePostProcessor postProcessor)
		throws JmsException {
		send(destinationName, getMessageCreatorWithPostProcessor(message, postProcessor));
	}

	private MessageConverter getRequiredMessageConverter() throws IllegalStateException {
		MessageConverter converter = getMessageConverter();
		if (converter == null) {
			throw new IllegalStateException("No 'messageConverter' specified. Check configuration of JmsTemplate.");
		}
		return converter;
	}

	private MessageCreator defaultMessageCreator(Object message) {
		return session -> {
			final Message msg = getRequiredMessageConverter().toMessage(message, session);
			setDefaultMessageProperties(msg);
			return msg;
		};
	}

	private MessageCreator getMessageCreatorWithPostProcessor(Object message, MessagePostProcessor postProcessor) {
		return session -> {
			final Message msg = getRequiredMessageConverter().toMessage(message, session);
			setDefaultMessageProperties(msg);
			return postProcessor.postProcessMessage(msg);
		};
	}

	private void setDefaultMessageProperties(Message message) throws JMSException {
		if(isExplicitQosEnabled()){
			message.setJMSDeliveryMode(getDeliveryMode());
			message.setJMSExpiration(getTimeToLive());
			message.setJMSPriority(getPriority());
		}
	}
}
