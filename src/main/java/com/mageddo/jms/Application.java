
package com.mageddo.jms;

import com.mageddo.jms.queue.CompleteQueue;
import com.mageddo.jms.queue.QueueEnum;
import com.mageddo.jms.vo.Color;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;

import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import javax.jms.Session;
import java.util.Arrays;
import java.util.concurrent.Executors;

@SpringBootApplication
@EnableJms
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@Configuration
@EnableAutoConfiguration
public class Application implements SchedulingConfigurer {

	@Autowired
	ActiveMQConnectionFactory activeMQConnectionFactory;

	@Autowired
	PooledConnectionFactory pooledConnectionFactory;

	@Autowired
	ConfigurableBeanFactory beanFactory;

	@Autowired
	DefaultJmsListenerContainerFactoryConfigurer configurer;

//	@Bean("placebo")
	@PostConstruct
//	public Object setupQueues(ActiveMQConnectionFactory activeMQConnectionFactory,
//		PooledConnectionFactory pooledConnectionFactory, ConfigurableBeanFactory beanFactory,
//		DefaultJmsListenerContainerFactoryConfigurer configurer){
	public void setupQueues(){

		for (QueueEnum queueEnum : QueueEnum.values()) {
			declareQueue(queueEnum, activeMQConnectionFactory, pooledConnectionFactory, beanFactory, configurer);
		}
//		return new Object();
	}

	private DefaultJmsListenerContainerFactory declareQueue(QueueEnum queueEnum,
				ActiveMQConnectionFactory connectionFactory, PooledConnectionFactory pooledConnectionFactory,
				ConfigurableBeanFactory beanFactory, DefaultJmsListenerContainerFactoryConfigurer configurer) {

		final CompleteQueue queue = queueEnum.getQueue();
		final RedeliveryPolicy rp = new RedeliveryPolicy();
		rp.setInitialRedeliveryDelay(queue.getTTL());
		rp.setMaximumRedeliveryDelay(queue.getTTL());
		rp.setRedeliveryDelay(queue.getTTL());
		rp.setBackOffMultiplier(2.0);
		rp.setMaximumRedeliveries(queue.getRetries());
		rp.setDestination(queueEnum.getDlq());

		// setup redelivery policy
		connectionFactory.getRedeliveryPolicyMap().put(queue, rp);
		connectionFactory.setTrustedPackages(Arrays.asList(Color.class.getPackage().getName()));


		final DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory(){
			@Override
			protected DefaultMessageListenerContainer createContainerInstance() {
				return container;
			}
		};

		container.setConnectionFactory(pooledConnectionFactory);
		container.setConcurrentConsumers(queue.getConsumers());
		container.setMaxConcurrentConsumers(queue.getMaxConsumers());
		container.setIdleConsumerLimit(queue.getConsumers());
		container.setSessionTransacted(true);
		container.setErrorHandler(t -> {});
		container.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);

		configurer.configure(factory, pooledConnectionFactory);
		beanFactory.registerSingleton(queue.getFactory() + "Container", container);
		beanFactory.registerSingleton(queue.getFactory() + "Factory", factory);
		return factory;
	}


	@Primary
	@Bean
	@ConfigurationProperties(prefix = "spring.activemq.pool")
//	@ConfigurationProperties(prefix = "spring.activemq.pool.configuration")
	public PooledConnectionFactory pooledConnectionFactory(ActiveMQConnectionFactory activeMQConnectionFactory){

		final PooledConnectionFactory cf = new PooledConnectionFactory();
		cf.setConnectionFactory(activeMQConnectionFactory);
		return cf;
	}
//
	@Bean
	@ConfigurationProperties(prefix = "spring.activemq")
	public ActiveMQConnectionFactory activeMQConnectionFactory(ActiveMQProperties properties){
		final ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(
			properties.getUser(), properties.getPassword(), properties.getBrokerUrl()
		);
		if(properties.getPackages().getTrustAll()){
			cf.setTrustAllPackages(true);
		}
		return cf;
	}

	@Bean
	@ConfigurationProperties(prefix = "spring.activemq")
	public ActiveMQProperties activeMQProperties(){
		return new ActiveMQProperties();
	}

//	@Bean
////	@ConfigurationProperties(prefix = "spring.activemq.pool.configuration")
//	public ActiveMQConnectionFactory activeMQConnectionFactory(PooledConnectionFactory pooledConnectionFactory){
//		return (ActiveMQConnectionFactory) pooledConnectionFactory.getConnectionFactory();
//	}
//
	@Primary
	@Bean
	public JmsTemplate jmsTemplate(PooledConnectionFactory connectionFactory){
		final JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
		jmsTemplate.setSessionTransacted(true);
		return jmsTemplate;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(Executors.newScheduledThreadPool(50));
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
