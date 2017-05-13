
package com.mageddo.jms;

import com.mageddo.jms.queue.CompleteQueue;
import com.mageddo.jms.queue.QueueEnum;
import com.mageddo.jms.vo.Color;
import org.apache.activemq.*;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
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

	@Autowired
	private ActiveMQConnectionFactory connectionFactory;

	@Autowired
	private DefaultJmsListenerContainerFactoryConfigurer configurer;


	@PostConstruct
	public void setupQueues(){
		for (QueueEnum queueEnum : QueueEnum.values()) {
			declareQueue(queueEnum);
		}
	}

	private DefaultJmsListenerContainerFactory declareQueue(QueueEnum queueEnum) {

		final CompleteQueue queue = queueEnum.getQueue();

		final RedeliveryPolicy rp = new RedeliveryPolicy();
		rp.setInitialRedeliveryDelay(queue.getTTL());
		rp.setMaximumRedeliveryDelay(queue.getTTL());
		rp.setBackOffMultiplier(2.0);
		rp.setMaximumRedeliveries(queue.getRetries());
		rp.setDestination(queueEnum.getDlq());

		connectionFactory.getRedeliveryPolicyMap().put(queue, rp);
		connectionFactory.setTrustedPackages(Arrays.asList(Color.class.getPackage().getName()));


		final DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory(){
			@Override
			protected DefaultMessageListenerContainer createContainerInstance() {
				return container;
			}
		};

		container.setConnectionFactory(connectionFactory);
		container.setConcurrentConsumers(queue.getConsumers());
		container.setMaxConcurrentConsumers(queue.getMaxConsumers());
		container.setSessionTransacted(true);
		container.setErrorHandler(t -> {});

//		cf.setConnectResponseTimeout(5000);
//		cf.setSendTimeout(5000);
//		cf.setOptimizeAcknowledgeTimeOut(5000);
//		cf.setCloseTimeout(5000);

		configurer.configure(factory, connectionFactory);
		beanFactory.registerSingleton(queue.getName() + "Container", container);
		beanFactory.registerSingleton(queue.getName() + "Factory", factory);
		return factory;
	}

	public static void main(String[] args) {
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
