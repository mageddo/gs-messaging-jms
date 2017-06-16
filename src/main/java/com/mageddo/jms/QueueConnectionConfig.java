package com.mageddo.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Created by elvis on 16/06/17.
 */
@Configuration
public class QueueConnectionConfig {

	private static final String DEFAULT_EMBEDDED_BROKER_URL = "vm://localhost?broker.persistent=false";
	private static final String DEFAULT_NETWORK_BROKER_URL = "tcp://localhost:61616";

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
			properties.getUser(), properties.getPassword(), determineBrokerUrl(properties)
		);
		cf.setUseAsyncSend(true);
		cf.setDispatchAsync(true);
		cf.setUseCompression(true);
		return cf;
	}

	private String determineBrokerUrl(ActiveMQProperties properties) {
		if (properties.getBrokerUrl() != null) {
			return properties.getBrokerUrl();
		}
		if (properties.isInMemory()) {
			return DEFAULT_EMBEDDED_BROKER_URL;
		}
		return DEFAULT_NETWORK_BROKER_URL;
	}


}
