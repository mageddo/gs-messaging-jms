package com.mageddo.jms.utils;

import com.mageddo.jms.queue.CompleteDestination;
import com.mageddo.jms.queue.config.MageddoMessageListenerContainerFactory;
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

	public static String getContainerName(CompleteDestination destination){
		return getPrefixId(destination) + "Container";
	}

	public static String getFactoryName(CompleteDestination destination) {
		return getPrefixId(destination) + "Factory";
	}

	private static String getPrefixId(CompleteDestination destination) {
		String prefix = destination.getId();
		if(prefix == null){
			prefix = destination.getName();
		}
		return prefix;
	}

	public static RedeliveryPolicy configureRedelivery(ActiveMQConnectionFactory connectionFactory, CompleteDestination dest){
		final RedeliveryPolicy rp = createRedeliveryPolicy(dest, dest.getDLQ());
		connectionFactory
			.getRedeliveryPolicyMap()
				.put(dest.getDestination(), rp);
		return rp;
	}

	public static MageddoMessageListenerContainerFactory createDefaultFactory(ConnectionFactory connectionFactory,
			CompleteDestination destination){

		final DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setBeanName(destination.getName());
		final MageddoMessageListenerContainerFactory factory = new MageddoMessageListenerContainerFactory(container);

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

	public static ActiveMQConnectionFactory configureConnectionFactory(ActiveMQConnectionFactory connectionFactory,
																																		 CompleteDestination destination) {
		// setup redelivery before copy to replicate this rules
		configureRedelivery(connectionFactory, destination);

		// making a copy of the connection because the settings below only are relevant to his consumer
		connectionFactory = connectionFactory.copy();
		connectionFactory.setNonBlockingRedelivery(destination.isNonBlockingRedelivery());
		connectionFactory.setUseAsyncSend(destination.isAsyncSend());
		return connectionFactory;
	}

}
