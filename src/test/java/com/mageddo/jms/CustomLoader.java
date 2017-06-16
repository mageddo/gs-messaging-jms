package com.mageddo.jms;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.context.ApplicationContext;

/**
 * Created by elvis on 16/06/17.
 */
public class CustomLoader extends SpringBootContextLoader {

	@Override
	public ApplicationContext loadContext(String... locations) throws Exception {
		final BrokerService broker = BrokerFactory.createBroker("xbean:activemq.xml");
		broker.start();
		return super.loadContext(locations);
	}
}
