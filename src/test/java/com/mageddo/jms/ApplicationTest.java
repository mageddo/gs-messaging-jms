package com.mageddo.jms;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by elvis on 16/06/17.
 */
@Configuration
@Import(Application.class)
//@ImportResource("classpath:activemq.xml")
public class ApplicationTest {
	public static void main(String[] args) throws Exception {
		final BrokerService broker = BrokerFactory.createBroker("xbean:activemq.xml");
		broker.start();
	}
}
