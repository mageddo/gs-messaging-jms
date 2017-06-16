package com.mageddo.jms.queue.config;

import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import java.lang.reflect.Method;

/**
 * Created by elvis on 15/06/17.
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
			producer.send(message, message.getJMSDeliveryMode(), message.getJMSPriority(), getTimeToLive());
		} else {
			producer.send(message);
		}
	}
}
