package com.mageddo.jms.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mageddo.jms.queue.CompleteDestination;
import com.mageddo.jms.config.MageddoMessageListenerContainerFactory;
import com.mageddo.jms.queue.DestinationEnum;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.*;
import java.util.Properties;

/**
 * Created by elvis on 21/05/17.
 */
public class QueueUtils {

	private static final Logger logger = LoggerFactory.getLogger(QueueUtils.class);

	public static RedeliveryPolicy createRedeliveryPolicy(CompleteDestination queue, ActiveMQQueue dlq){

		final RedeliveryPolicy rp = new RedeliveryPolicy();
		rp.setInitialRedeliveryDelay(queue.getTTL());
		rp.setMaximumRedeliveryDelay(queue.getTTL());
		rp.setRedeliveryDelay(queue.getTTL());
		rp.setBackOffMultiplier(2.0);
		rp.setMaximumRedeliveries(queue.getRetries());
		rp.setDestination(dlq);
		return rp;
	}

	public static String getContainerName(CompleteDestination completeDestination){
		return completeDestination.getFactory() + "Container";
	}

	public static String getFactoryName(CompleteDestination destination) {
		return destination.getFactory() + "Factory";
	}

	public static RedeliveryPolicy configureRedelivery(ActiveMQConnectionFactory connectionFactory, DestinationEnum destinationEnum){
		final RedeliveryPolicy rp = createRedeliveryPolicy(destinationEnum.getCompleteDestination(), destinationEnum.getDlq());
		connectionFactory
			.getRedeliveryPolicyMap()
				.put(destinationEnum.getDestination(), rp);
		return rp;
	}

	public static MageddoMessageListenerContainerFactory createDefaultFactory(ConnectionFactory connectionFactory,
			CompleteDestination destination){

		final DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setBeanName(destination.getName());
		final MageddoMessageListenerContainerFactory factory = new MageddoMessageListenerContainerFactory(container,
			QueueUtils.getFactoryName(destination)
		);

		createContainer(connectionFactory, destination, container);
		return factory;
	}

	public static DefaultMessageListenerContainer createContainer(ActiveMQConnectionFactory connectionFactory, CompleteDestination destination) {
		return createContainer(connectionFactory, destination, new DefaultMessageListenerContainer());
	}

	public static DefaultMessageListenerContainer createContainer(ConnectionFactory connectionFactory,
								CompleteDestination destination, DefaultMessageListenerContainer container) {
		container.setConnectionFactory(connectionFactory);
		container.setConcurrentConsumers(destination.getConsumers());
		container.setMaxConcurrentConsumers(destination.getMaxConsumers());
		container.setIdleConsumerLimit(destination.getConsumers());
		container.setErrorHandler(t -> {
			logger.error("msg={}", t.getMessage(), t);
		});
		container.setSessionTransacted(true);
		container.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
		return container;
	}

	public static ActiveMQDestination queue(String name){
		return queue(name, null);
	}

	public static ActiveMQDestination queue(String name, Properties properties){
		return destination(new ActiveMQQueue(name), properties);
	}

	public static ActiveMQDestination topic(String name){
		return topic(name, null);
	}

	public static ActiveMQDestination topic(String name, Properties properties){
		return destination(new ActiveMQTopic(name), properties);
	}

	public static ActiveMQDestination destination(ActiveMQDestination destination, Properties properties){
		return destination;
	}

	public static ActiveMQConnectionFactory configureNoBlockRedelivery(ActiveMQConnectionFactory connectionFactory,
																																		 CompleteDestination destination) {
		if(destination.isNonBlockingRedelivery()){
			connectionFactory = connectionFactory.copy();
			connectionFactory.setNonBlockingRedelivery(true);
		}
		return connectionFactory;
	}

}
