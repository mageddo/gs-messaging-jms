package com.mageddo.jms.receiver;

import com.mageddo.jms.queue.DestinationConstants;
import com.mageddo.jms.queue.DestinationEnum;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

import static com.mageddo.jms.utils.QueueUtils.configureRedelivery;
import static com.mageddo.jms.utils.QueueUtils.createContainer;

/**
 * Created by elvis on 21/05/17.
 */
@Component
public class PingReceiver {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private JmsTemplate jmsTemplate;

	private AtomicInteger ip = new AtomicInteger(1);

	@Scheduled(fixedDelay = 1 * 1000 )
	public void pinger(){
		jmsTemplate.convertAndSend(DestinationEnum.PING.getDestination(), String.valueOf(ip.getAndIncrement()));
	}

	public void consume(String ip){
		logger.info("status=success, ip={}", ip);
	}


	@Bean(name = DestinationConstants.FACTORY_PING + "Container", initMethod = "start", destroyMethod = "stop")
	public DefaultMessageListenerContainer container(ActiveMQConnectionFactory cf,
																																		 PingReceiver receiver){

		final DestinationEnum queue = DestinationEnum.PING;
		final DefaultMessageListenerContainer container = createContainer(cf, queue.getCompleteDestination());
		container.setDestination(queue.getDestination());

		configureRedelivery(cf, queue);

		final MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(receiver);
		listenerAdapter.setDefaultListenerMethod("consume");
		container.setMessageListener(listenerAdapter);

		return container;

	}
}
