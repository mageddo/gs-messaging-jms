package com.mageddo.jms.receiver;

import com.mageddo.jms.queue.DestinationConstants;
import com.mageddo.jms.queue.DestinationEnum;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageNotWriteableException;
import javax.jms.Session;
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

//	@Scheduled(fixedDelay = 1 * 10000 )
	public void pinger() throws MessageNotWriteableException {

		jmsTemplate.convertAndSend(DestinationEnum.PING.getDestination(), String.valueOf(ip.getAndIncrement()));

	}

	public void consume(String ipMsg) throws JMSException {
		boolean error = true;
		if(error){
		logger.error("status=error, ip={}", ipMsg);
			throw new RuntimeException(ipMsg);
		}
		logger.info("status=success, ip={}", ipMsg);
	}


	@Bean(name = "#{queue.get('PING').getContainer()}", initMethod = "start", destroyMethod = "stop")
	public DefaultMessageListenerContainer container(ActiveMQConnectionFactory cf, PingReceiver receiver){

		final DestinationEnum queue = DestinationEnum.PING;
		final DefaultMessageListenerContainer container = createContainer(cf, queue.getCompleteDestination());
		container.setDestination(queue.getDestination());

		configureRedelivery(cf, queue.getCompleteDestination());

		final MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(receiver);
		listenerAdapter.setDefaultListenerMethod("consume");
		listenerAdapter.setMessageConverter(new SimpleMessageConverter(){
			@Override
			public Object fromMessage(Message message) throws JMSException, MessageConversionException {
				return super.fromMessage(message);
			}
		});
		container.setMessageListener(listenerAdapter);

		return container;

	}
}
