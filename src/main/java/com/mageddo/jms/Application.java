
package com.mageddo.jms;

import com.mageddo.jms.queue.CompleteDestination;
import com.mageddo.jms.config.MageddoMessageListenerContainerFactory;
import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.utils.QueueUtils;
import com.mageddo.jms.vo.Color;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import javax.jms.Session;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

		activeMQConnectionFactory.setTrustedPackages(Arrays.asList(Color.class.getPackage().getName()));
		{
			final StringBuilder sql = new StringBuilder();
			sql.append("DROP TABLE IF EXISTS DESTINATION_PARAMETER; \n");
			sql.append("CREATE TABLE DESTINATION_PARAMETER ( \n");
			sql.append("	IDT_DESTINATION_PARAMETER INT AUTO_INCREMENT, \n");
			sql.append("	NAM_DESTINATION_PARAMETER VARCHAR(255) UNIQUE, \n");
			sql.append("	NUM_CONSUMERS TINYINT NOT NULL, \n");
			sql.append("	NUM_MAX_CONSUMERS TINYINT NOT NULL, \n");
			sql.append("	NUM_TTL INT NOT NULL, \n");
			sql.append("	NUM_RETRIES TINYINT NOT NULL, \n");
			sql.append("	DAT_CREATION TIMESTAMP NOT NULL, \n");
			sql.append("	DAT_UPDATE TIMESTAMP NOT NULL, \n");
			sql.append("	PRIMARY KEY(IDT_DESTINATION_PARAMETER) \n");
			sql.append("); \n");

			jdbcTemplate.execute(sql.toString());
		}
		for (final DestinationEnum destinationEnum : DestinationEnum.values()) {

			if(destinationEnum.isAutoDeclare()){
				declareQueue(destinationEnum, activeMQConnectionFactory, activeMQConnectionFactory, beanFactory, configurer);
			}

			final StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO DESTINATION_PARAMETER \n");
			sql.append("( \n");
			sql.append("   NAM_DESTINATION_PARAMETER,NUM_CONSUMERS,NUM_MAX_CONSUMERS,NUM_TTL,NUM_RETRIES,DAT_CREATION,DAT_UPDATE \n");
			sql.append(") \n");
			sql.append("SELECT \n");
			sql.append("* \n");
			sql.append("FROM ( SELECT \n");
			sql.append("'%s' NAM_DESTINATION_PARAMETER, \n");
			sql.append("%d NUM_CONSUMERS, \n");
			sql.append("%d NUM_MAX_CONSUMERS, \n");
			sql.append("%d NUM_TTL, \n");
			sql.append("%d NUM_RETRIES, \n");
			sql.append("'%6$tY-%6$tm-%6$td' DAT_CREATION, \n");
			sql.append("'%7$tY-%7$tm-%7$td' DAT_UPDATE \n");
			sql.append(") X \n");
			sql.append("WHERE NOT EXISTS \n");
			sql.append("( \n");
			sql.append("   SELECT 1 \n");
			sql.append("   FROM DESTINATION_PARAMETER \n");
			sql.append("   WHERE NAM_DESTINATION_PARAMETER = ? \n");
			sql.append(") \n");


			final CompleteDestination dest = destinationEnum.getCompleteDestination();
			jdbcTemplate.update(String.format(sql.toString(), dest.getName(), dest.getConsumers(), dest.getMaxConsumers(),
				dest.getTTL(), dest.getRetries(), new Date(), new Date()), dest.getName()
			);

		}

		jdbcTemplate.execute("DROP TABLE mail IF EXISTS");
		jdbcTemplate.execute("CREATE TABLE mail( id SERIAL, message VARCHAR(255) )");

	}

	private MageddoMessageListenerContainerFactory declareQueue(
			DestinationEnum destinationEnum,
			ActiveMQConnectionFactory activeMQConnectionFactory, ConnectionFactory cf,
			ConfigurableBeanFactory beanFactory, DefaultJmsListenerContainerFactoryConfigurer configurer
	) {

		final CompleteDestination destination = destinationEnum.getCompleteDestination();

		final MageddoMessageListenerContainerFactory factory = QueueUtils.createDefaultFactory(
			activeMQConnectionFactory, destination
		);
		QueueUtils.configureRedelivery(activeMQConnectionFactory, destinationEnum);
		configurer.configure(factory, cf);
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
		if(properties.getPackages().getTrustAll()){
			cf.setTrustAllPackages(true);
		}
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
	public JmsTemplate jmsTemplate(PooledConnectionFactory connectionFactory){
		final JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
		jmsTemplate.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
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
