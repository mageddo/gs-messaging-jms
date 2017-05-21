
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
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
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
import javax.jms.ConnectionFactory;
import javax.jms.Session;
import java.util.Arrays;
import java.util.concurrent.Executors;

@EnableScheduling
@EnableTransactionManagement
@EnableJms
@EnableAsync
@EnableAspectJAutoProxy

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
public class Application implements SchedulingConfigurer {

	@Autowired
	ActiveMQConnectionFactory activeMQConnectionFactory;

	@Autowired
	ConfigurableBeanFactory beanFactory;

	@Autowired
	DefaultJmsListenerContainerFactoryConfigurer configurer;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void setupQueues(){

		for (QueueEnum queueEnum : QueueEnum.values()) {
			declareQueue(queueEnum, activeMQConnectionFactory, activeMQConnectionFactory, beanFactory, configurer);
		}

		jdbcTemplate.execute("DROP TABLE mail IF EXISTS");
		jdbcTemplate.execute("CREATE TABLE mail( id SERIAL, message VARCHAR(255) )");

	}

	private DefaultJmsListenerContainerFactory declareQueue(QueueEnum queueEnum,
				ActiveMQConnectionFactory activeMQCf, ConnectionFactory cf,
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
		activeMQCf.getRedeliveryPolicyMap().put(queue, rp);
		activeMQCf.setTrustedPackages(Arrays.asList(Color.class.getPackage().getName()));


		final DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory(){
			@Override
			protected DefaultMessageListenerContainer createContainerInstance() {
				return container;
			}
		};

		container.setConnectionFactory(cf);
		container.setConcurrentConsumers(queue.getConsumers());
		container.setMaxConcurrentConsumers(queue.getMaxConsumers());
		container.setIdleConsumerLimit(queue.getConsumers());
		container.setErrorHandler(t -> {});
		container.setSessionTransacted(true);
		container.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);

		configurer.configure(factory, cf);
		beanFactory.registerSingleton(queue.getFactory() + "Container", container);
		beanFactory.registerSingleton(queue.getFactory() + "Factory", factory);
		return factory;
	}


	@Primary
	@Bean
	@ConfigurationProperties(prefix = "spring.activemq.pool")
	public PooledConnectionFactory pooledConnectionFactory(ActiveMQConnectionFactory activeMQConnectionFactory){

		final PooledConnectionFactory cf = new PooledConnectionFactory();
		cf.setConnectionFactory(activeMQConnectionFactory);
		return cf;
	}

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

	@Primary
	@Bean
	public JmsTemplate jmsTemplate(PooledConnectionFactory connectionFactory){
		final JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
		jmsTemplate.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
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
