
package com.mageddo.jms;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.mageddo.jms.queue.config.FlexibleJmsTemplate;
import com.mageddo.jms.queue.config.MageddoMessageListenerContainerFactory;
import com.mageddo.jms.enums.CacheNames;
import com.mageddo.jms.queue.CompleteDestination;
import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.queue.converter.DefaultMessageConverter;
import com.mageddo.jms.service.DestinationParameterService;
import com.mageddo.jms.utils.QueueUtils;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.*;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@EnableScheduling
@EnableCaching
@EnableTransactionManagement
@EnableJms
@EnableAspectJAutoProxy
@EnableAutoConfiguration
@EnableMBeanExport

@SpringBootApplication
@Configuration
public class Application implements SchedulingConfigurer {

	@Autowired
	ActiveMQConnectionFactory activeMQConnectionFactory;

	@Autowired
	ConfigurableBeanFactory beanFactory;

	@Autowired
	DefaultJmsListenerContainerFactoryConfigurer configurer;

	@Autowired
	DestinationParameterService destinationParameterService;

	@Autowired
	MessageConverter messageConverter;

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
		cf.setUseAsyncSend(true);
		cf.setDispatchAsync(true);
		cf.setUseCompression(true);
		return cf;
	}

	@Bean
	@ConfigurationProperties(prefix = "spring.activemq")
	public ActiveMQProperties activeMQProperties(){
		return new ActiveMQProperties();
	}

	@Primary
	@Bean
	public JmsTemplate jmsTemplate(PooledConnectionFactory connectionFactory, MessageConverter messageConverter){
		final JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
//		jmsTemplate.setExplicitQosEnabled(true);
		jmsTemplate.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
		jmsTemplate.setSessionTransacted(true);
		jmsTemplate.setMessageConverter(messageConverter);
		return jmsTemplate;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(Executors.newScheduledThreadPool(50));
	}

	@Bean
	public MBeanExporter jdbcPoolJMX(DataSource dataSource){
		final MBeanExporter beanExporter = new MBeanExporter();
		Map<String, Object> map = new HashMap<>();
		map.put("bean:name=DataSource", dataSource.getPool().getJmxPool());
		beanExporter.setBeans(map);
		return beanExporter;
	}

	@Primary
	@Bean
	public MessageConverter jsonJmsMessageConverter(ObjectMapper objectMapper) {
		return new DefaultMessageConverter(objectMapper);
	}

	@Bean
	public RestTemplate restTemplate(){
		return new RestTemplate();
	}

	@Bean
	public Cache cacheOne() {
		return new ConcurrentMapCache(
			CacheNames.ACTIVE_MQ,
			CacheBuilder
				.newBuilder()
				.expireAfterWrite(5, TimeUnit.MINUTES)
				.maximumSize(100)
				.build()
				.asMap(),
			true
		);
	}


	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
