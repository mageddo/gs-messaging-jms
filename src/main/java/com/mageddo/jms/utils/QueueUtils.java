package com.mageddo.jms.utils;

import com.mageddo.jms.queue.CompleteDestination;
import com.mageddo.jms.config.MageddoMessageListenerContainerFactory;
import com.mageddo.jms.queue.DestinationEnum;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.catalina.util.CustomObjectInputStream;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by elvis on 21/05/17.
 */
public class QueueUtils {

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

	public static void configureRedelivery(ActiveMQConnectionFactory connectionFactory, DestinationEnum destinationEnum){
		connectionFactory
			.getRedeliveryPolicyMap()
				.put(destinationEnum.getDestination(), createRedeliveryPolicy(destinationEnum.getCompleteDestination(), destinationEnum.getDlq()));
	}

	public static MageddoMessageListenerContainerFactory createDefaultFactory(ConnectionFactory connectionFactory,
			CompleteDestination destination){

		final DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		final MageddoMessageListenerContainerFactory factory = new MageddoMessageListenerContainerFactory(container,
			QueueUtils.getFactoryName(destination)
		);

		createContainer(connectionFactory, destination, container);
		return factory;
	}

	public static DefaultMessageListenerContainer createContainer(ConnectionFactory connectionFactory, CompleteDestination destination) {
		return createContainer(connectionFactory, destination, new DefaultMessageListenerContainer());
	}

	public static DefaultMessageListenerContainer createContainer(ConnectionFactory connectionFactory,
								CompleteDestination destination, DefaultMessageListenerContainer container) {
		container.setConnectionFactory(connectionFactory);
		container.setConcurrentConsumers(destination.getConsumers());
		container.setMaxConcurrentConsumers(destination.getMaxConsumers());
		container.setIdleConsumerLimit(destination.getConsumers());
		container.setErrorHandler(t -> {});
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
}
