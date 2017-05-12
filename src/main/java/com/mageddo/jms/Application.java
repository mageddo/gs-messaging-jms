
package com.mageddo.jms;

import com.mageddo.jms.vo.Color;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

@SpringBootApplication
@EnableJms
@EnableScheduling
public class Application {

	@Bean
	public JmsListenerContainerFactory<?> mailContainer(ActiveMQConnectionFactory connectionFactory,
																											DefaultJmsListenerContainerFactoryConfigurer configurer) {

		connectionFactory.setTrustedPackages(Arrays.asList(Color.class.getPackage().getName()));
		final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory(){
			@Override
			protected void initializeContainer(DefaultMessageListenerContainer container) {
				container.setConnectionFactory(connectionFactory);
				container.setConcurrentConsumers(2);
				container.setMaxConcurrentConsumers(5);
				container.setIdleConsumerLimit(2);
			}
		};

		// This provides all boot's default to this factory, including the message converter
		configurer.configure(factory, connectionFactory);

		return factory;
	}
	@Bean
	public JmsListenerContainerFactory<?> redColorContainer(ActiveMQConnectionFactory connectionFactory,
																													DefaultJmsListenerContainerFactoryConfigurer configurer) {

		connectionFactory.setTrustedPackages(Arrays.asList(Color.class.getPackage().getName()));
		final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory(){
			@Override
			protected void initializeContainer(DefaultMessageListenerContainer container) {
				container.setConnectionFactory(connectionFactory);
				container.setConcurrency("1-5");
//				container.setConcurrentConsumers(2);
//				container.setMaxConcurrentConsumers(5);
//				container.setIdleConsumerLimit(2);
//				container.setPubSubDomain(true);
//				container.setSubscriptionDurable(true);
//				container.setClientId("redColorClient");
			}
		};

		// This provides all boot's default to this factory, including the message converter
		configurer.configure(factory, connectionFactory);

		return factory;
	}

	@Bean
	public JmsListenerContainerFactory<?> colorContainer(ActiveMQConnectionFactory connectionFactory,
																													DefaultJmsListenerContainerFactoryConfigurer configurer) {

		connectionFactory.setTrustedPackages(Arrays.asList(Color.class.getPackage().getName()));
		final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory(){
			@Override
			protected void initializeContainer(DefaultMessageListenerContainer container) {
				container.setConnectionFactory(connectionFactory);
				container.setConcurrency("1-2");
//				container.setConcurrentConsumers(1);
//				container.setMaxConcurrentConsumers(2);
//				container.setIdleConsumerLimit(2);
//				container.setPubSubDomain(true);
//				container.setSubscriptionDurable(true);
//				container.setClientId("colorClient");
			}
		};

		// This provides all boot's default to this factory, including the message converter
		configurer.configure(factory, connectionFactory);

		return factory;
	}

//	@Bean
//	public ActiveMQConnectionFactory activeMQConnectionFactory(ConnectionFactory connectionFactory) {
//		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
//		factory.setTrustedPackages(Arrays.asList(Color.class.getPackage().getName()));
//		return factory;
//	}


//	@Bean // Serialize message content to json using TextMessage
//	public MessageConverter jacksonJmsMessageConverter() {
//		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter(){
//			@Override
//			protected Message toMessage(Object object, Session session, ObjectWriter objectWriter)
//				throws JMSException, MessageConversionException {
//
//				return super.toMessage(object, session, objectWriter);
//			}
//
//			@Override
//			public Message toMessage(Object object, Session session, Class<?> jsonView) throws JMSException, MessageConversionException {
//				return super.toMessage(object, session, jsonView);
//			}
//
//			@Override
//			public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
//				if (object instanceof ObjectMessage){
//
//					final ActiveMQObjectMessage objectMessage = (ActiveMQObjectMessage) object;
//					final TextMessage convertedMessage = (TextMessage) super.toMessage(
//						objectMessage.getObject(), session
//					);
//					final ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
//					textMessage.setText(convertedMessage.getText());
//					try {
//						textMessage.compress();
//						textMessage.setProperties(objectMessage.getProperties());
//					} catch (IOException e) {
//						throw new RuntimeException(e);
//					}
//					textMessage.setty
//					return textMessage;
//
//				}
//				return super.toMessage(object, session);
//			}
//
//			@Override
//			public Message toMessage(Object object, Session session, Object conversionHint) throws JMSException, MessageConversionException {
//				return super.toMessage(object, session, conversionHint);
//			}
//		};
//
//
//		converter.setTargetType(MessageType.TEXT);
//		converter.setTypeIdPropertyName("_type");
//		return converter;
//	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
