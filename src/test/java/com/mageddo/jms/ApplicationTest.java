package com.mageddo.jms;

import com.mageddo.jms.queue.CompleteDestination;
import com.mageddo.jms.queue.MailQueueTest;
import com.mageddo.jms.queue.config.MageddoMessageListenerContainerFactory;
import com.mageddo.jms.utils.QueueUtils;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

/**
 * Created by elvis on 16/06/17.
 */
@Configuration
@Import(Application.class)
//@ImportResource("classpath:activemq.xml")
public class ApplicationTest {

	@Bean(name = "queueCFactory")
	public DefaultJmsListenerContainerFactory queueCFactory(ActiveMQConnectionFactory cf){

		final CompleteDestination destination = MailQueueTest.QUEUE_C;
		cf = QueueUtils.configureConnectionFactory(cf, destination);
		final MageddoMessageListenerContainerFactory factory = QueueUtils.createDefaultFactory(
			cf, destination
		);
		QueueUtils.configureRedelivery(cf, destination);
		return factory;
	}

}
