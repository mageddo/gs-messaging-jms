package com.mageddo.jms;

import com.mageddo.jms.queue.CompleteDestination;
import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.queue.config.MageddoMessageListenerContainerFactory;
import com.mageddo.jms.service.DestinationParameterService;
import com.mageddo.jms.utils.QueueUtils;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MessageConverter;

import javax.annotation.PostConstruct;

/**
 * Created by elvis on 16/06/17.
 */
@Configuration
public class QueueConfig {

	@Autowired
	private ActiveMQConnectionFactory activeMQConnectionFactory;

	@Autowired
	private ConfigurableBeanFactory beanFactory;

	@Autowired
	private DefaultJmsListenerContainerFactoryConfigurer configurer;

	@Autowired
	private DestinationParameterService destinationParameterService;

	@Autowired
	private MessageConverter messageConverter;

	@PostConstruct
	public void setupQueues(){

		for (final DestinationEnum destinationEnum : DestinationEnum.values()) {

			if(destinationEnum.isAutoDeclare()){
				declareQueue(destinationEnum, activeMQConnectionFactory, beanFactory, configurer);
			}
			destinationParameterService.createDestinationParameterIfNotExists(destinationEnum.getCompleteDestination());

		}

	}

	private MageddoMessageListenerContainerFactory declareQueue(
		DestinationEnum destinationEnum,
		ActiveMQConnectionFactory connectionFactory,
		ConfigurableBeanFactory beanFactory, DefaultJmsListenerContainerFactoryConfigurer configurer
	) {
		final CompleteDestination destination = destinationEnum.getCompleteDestination();
		connectionFactory = QueueUtils.configureConnectionFactory(connectionFactory, destination);
		final MageddoMessageListenerContainerFactory factory = QueueUtils.createDefaultFactory(
			connectionFactory, destination
		);
		factory.setMessageConverter(messageConverter);

//		factory.setTransactionManager(txManager); // use too much database sessions
		QueueUtils.configureRedelivery(connectionFactory, destinationEnum);
//		configurer.configure(factory, cf); // dont use because it will override custom settings to global spring settings
		beanFactory.registerSingleton(QueueUtils.getContainerName(destination), factory.getContainer());
		beanFactory.registerSingleton(factory.getBeanName(), factory);
		return factory;
	}
}
