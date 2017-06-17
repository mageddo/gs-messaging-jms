package com.mageddo.jms;

import com.mageddo.jms.ApplicationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.ConnectionFactory;

/**
 * Created by elvis on 16/06/17.
 */

@SpringBootTest
@ContextConfiguration(classes = {ApplicationTest.class})
@RunWith(SpringRunner.class)
public class QueueTest {

	private static final String QUEUE_A = "queueA";

	@Autowired
	private ConnectionFactory connectionFactory;

	@Test
	public void sendMessageToDLQWhenItExpires() throws InterruptedException {

		final int ttl = 3000;
		final JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
		jmsTemplate.setExplicitQosEnabled(true);
		jmsTemplate.setTimeToLive(ttl);

		jmsTemplate.convertAndSend(QUEUE_A, "1");
		Thread.sleep(ttl + 1000);
		jmsTemplate.setReceiveTimeout(1000);

		// not in queue
		Assert.assertNull(jmsTemplate.receive(QUEUE_A));

		// but in DLQ
		Assert.assertNull(jmsTemplate.receive("DLQ." + QUEUE_A));
	}

}
