
package com.mageddo.jms;

import com.mageddo.jms.vo.Color;
import org.apache.activemq.*;
import org.apache.activemq.broker.region.policy.IndividualDeadLetterStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.command.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.jms.*;
import javax.jms.Message;
import java.lang.IllegalStateException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@SpringBootApplication
@EnableJms
@EnableScheduling
public class Application {

	@Autowired
	private ConfigurableBeanFactory beanFactory;


	@Bean
	public Object mailContainer(ActiveMQConnectionFactory cf,
																											DefaultJmsListenerContainerFactoryConfigurer configurer) throws Exception {
		final RedeliveryPolicy rp = new RedeliveryPolicy();
		rp.setInitialRedeliveryDelay(1000);
		rp.setMaximumRedeliveryDelay(10_000);
		rp.setBackOffMultiplier(2.0);
		rp.setMaximumRedeliveries(1);
		final ActiveMQQueue dlq = new ActiveMQQueue("dlq.mailbox");
		dlq.setDLQ();
		rp.setDestination(dlq);
		cf.getRedeliveryPolicyMap().put(new ActiveMQQueue("mailbox"), rp);

		cf.setTrustedPackages(Arrays.asList(Color.class.getPackage().getName()));
//		final DefaultMessageListenerContainer containerInstance = getContainer();
		final DefaultMessageListenerContainer containerInstance = new DefaultMessageListenerContainer();
		final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory(){


			@Override
			protected DefaultMessageListenerContainer createContainerInstance() {

				return containerInstance;
			}


			@Override
			protected void initializeContainer(DefaultMessageListenerContainer container) {

			}


		};

		containerInstance.setConnectionFactory(cf);
		containerInstance.setConcurrentConsumers(2);
		containerInstance.setMaxConcurrentConsumers(5);
		containerInstance.setIdleConsumerLimit(2);
//				container.setDestinationResolver();
//				container.setTaskExecutor();
//				container.setErrorHandler();
		containerInstance.setSessionTransacted(true);
		containerInstance.setErrorHandler(t -> {
////					System.out.println("err, msg=" + t.getMessage());
		});
//		final MessageListenerAdapter mailReceiver = new MessageListenerAdapter();
//		mailReceiver.setDefaultListenerMethod("consume");
//		containerInstance.setMessageListener(mailReceiver);

//		BrokerFactory.createBroker(cf.)
//		factory.createListenerContainer()
//		Broker broker = new EmptyBroker();
//		broker.con

		final IndividualDeadLetterStrategy dlqStrategy = new IndividualDeadLetterStrategy();
		dlqStrategy.setQueuePrefix("DLQ.");
		final PolicyEntry dlqPolicy = new PolicyEntry();
		dlqPolicy.setQueue("mailbox");
		dlqPolicy.setDeadLetterStrategy(dlqStrategy);



//		cf.setConnectResponseTimeout(5000);
//		cf.setSendTimeout(5000);
//		cf.setOptimizeAcknowledgeTimeOut(5000);
//		cf.setCloseTimeout(5000);

//		BrokerService brokerService = new BrokerService();
//		brokerService.setBrokerName("x");
//		brokerService.addConnector(cf.getBrokerURL());
//		brokerService.start();

//		rp.setQueue("DLQ.mailbox");
//		rp.setQueue("Consumer.colorClient.VirtualTopic.color");
//		final ActiveMQDestination mailDLQ = new ActiveMQDestination("DLQ.Cyz") {
//			@Override
//			public byte getDataStructureType() {
//				return 0;
//			}
//
//			@Override
//			protected String getQualifiedPrefix() {
//				return "DLQ.";
//			}
//
//			@Override
//			public byte getDestinationType() {
//				return 0;
//			}
//		};
		final ActiveMQDestination mailDLQ = ActiveMQDestination.createDestination("DLQ.Cyz", ActiveMQDestination.QUEUE_TYPE);
		mailDLQ.setDLQ();
		rp.setDestination(mailDLQ);
//		dlqPolicy.configure()

//		SimpleJmsListenerEndpoint x;
//		x.setupListenerContainer();
//		JmsTemplate jmsTemplate
//			jmsTemplate.



//		cf.setRedeliveryPolicy(rp);

		// This provides all boot's default to this factory, including the message converter
//		configurer.configure(factory, cf);


//		beanFactory.registerSingleton("mailDLQPolicy", dlqPolicy);
//		beanFactory.registerSingleton("mailDLQStg", dlqStrategy);
//		beanFactory.registerSingleton("mailDLQ", mailDLQ);
//		beanFactory.registerSingleton("mailRedeliveryPolicy", rp);

//		return factory;
//		containerInstance.setConnectionFactory(cf);
//		containerInstance.setDestination(new CustomDestinationImpl());
//		containerInstance.consu
//		containerInstance.start();
		return factory;
	}
	@Bean
	public JmsListenerContainerFactory<?> redColorContainer(ActiveMQConnectionFactory cf,
																													DefaultJmsListenerContainerFactoryConfigurer configurer) {

		cf.setTrustedPackages(Arrays.asList(Color.class.getPackage().getName()));
		final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory(){
			@Override
			protected void initializeContainer(DefaultMessageListenerContainer container) {
				container.setConnectionFactory(cf);
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
		configurer.configure(factory, cf);


//		cf.getRedeliveryPolicyMap().put();



//		cf.dlq

//		beanFactory.registerSingleton("colorRP", rp);
//		beanFactory.registerSingleton("colorDLQPolicy", dlqPolicy);
//		beanFactory.registerSingleton("dlqStrategy", dlqStrategy);



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
//		MessageDispatch x;
//		x.setde
		SpringApplication.run(Application.class, args);
	}

		/** The JMS 2.0 Session.createSharedConsumer method, if available */
		private static final Method createSharedConsumerMethod = ClassUtils.getMethodIfAvailable(
			Session.class, "createSharedConsumer", Topic.class, String.class, String.class);

		/** The JMS 2.0 Session.createSharedDurableConsumer method, if available */
		private static final Method createSharedDurableConsumerMethod = ClassUtils.getMethodIfAvailable(
			Session.class, "createSharedDurableConsumer", Topic.class, String.class, String.class);

	private DefaultMessageListenerContainer getContainer() {


		return new DefaultMessageListenerContainer(){

			protected void doExecuteListener(Session session, Message message) throws JMSException {
				if (!isAcceptMessagesWhileStopping() && !isRunning()) {
					if (logger.isWarnEnabled()) {
						logger.warn("Rejecting received message because of the listener container " +
							"having been stopped in the meantime: " + message);
					}
					rollbackIfNecessary(session);
					throw new RuntimeException();
				}

				try {
					invokeListener(session, message);
				}
				catch (Throwable ex) {

//							final ActiveMQMessage activeMQMessage = (ActiveMQMessage) message;
//							if(activeMQMessage.getRedeliveryCounter() == rp.getMaximumRedeliveries()){
//
//								session.createProducer(new ActiveMQQueue("DLQ." + activeMQMessage.getDestination().getPhysicalName()))
//									.send(message);
//
//							}else{
					rollbackOnExceptionIfNecessary(session, ex);
					throw ex;
//							}

				}
				commitIfNecessary(session, message);
			}

			protected MessageConsumer createConsumer(Session session, Destination destination) throws JMSException {
				if (isPubSubDomain() && destination instanceof Topic) {
					if (isSubscriptionShared()) {
						// createSharedConsumer((Topic) dest, subscription, selector);
						// createSharedDurableConsumer((Topic) dest, subscription, selector);
						Method method = (isSubscriptionDurable() ?
							createSharedDurableConsumerMethod : createSharedConsumerMethod);
						try {
							return (MessageConsumer) method.invoke(session, destination, getSubscriptionName(), getMessageSelector());
						}
						catch (InvocationTargetException ex) {
							if (ex.getTargetException() instanceof JMSException) {
								throw (JMSException) ex.getTargetException();
							}
							ReflectionUtils.handleInvocationTargetException(ex);
							return null;
						}
						catch (IllegalAccessException ex) {
							throw new IllegalStateException("Could not access JMS 2.0 API method: " + ex.getMessage());
						}
					}
					else if (isSubscriptionDurable()) {
						return session.createDurableSubscriber(
							(Topic) destination, getSubscriptionName(), getMessageSelector(), isPubSubNoLocal());
					}
					else {
						// Only pass in the NoLocal flag in case of a Topic (pub-sub mode):
						// Some JMS providers, such as WebSphere MQ 6.0, throw IllegalStateException
						// in case of the NoLocal flag being specified for a Queue.
						return session.createConsumer(destination, getMessageSelector(), isPubSubNoLocal());
					}
				}
				else {
//					if (session instanceof ActiveMQSession){
//						ActiveMQSession amqSession = (ActiveMQSession) session;
//						return amqSession.createConsumer(destination, getMessageSelector(), (MessageListener) getMessageListener());
//					}
					return session.createConsumer(destination, getMessageSelector());
				}
			}
		};
	}
}
