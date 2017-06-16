package com.mageddo.jms.queue;

import com.mageddo.jms.ApplicationTest;
import com.mageddo.jms.CustomLoader;
import org.apache.activemq.command.ActiveMQQueue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.Message;

/**
 * Created by elvis on 16/06/17.
 */

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ApplicationTest.class}, loader = CustomLoader.class)
public class MailQueueTest {

	/**
	 * This is a common queue, by default messages posted to commons queues with TTL, have you messages discard when it
	 * expires
	 */
	private static final CompleteDestination QUEUE_A = new CompleteDestination(new ActiveMQQueue("queueA"));

	/**
	 * Queues with PNP. prefix has the advante of do not be discard when it expires, instead this message will sent to DLQ
	 */
	private static final CompleteDestination QUEUE_B = new CompleteDestination(new ActiveMQQueue("PNP.queueB"));

	@Autowired
	private JmsTemplate jmsTemplate;

	@Test
	public void discardMessageWhenExpires() throws InterruptedException {
		final int ttl = 3000;
		jmsTemplate.convertAndSend(QUEUE_A.getDestination(), "1", msg -> {
			msg.setJMSExpiration(ttl);
			return msg;
		});
		Thread.sleep(ttl + 1000);
		jmsTemplate.setReceiveTimeout(1000);

		// not in DLQ
		Assert.assertNull(jmsTemplate.receive(QUEUE_A.getDLQ()));
		// not in queue
		Assert.assertNull(jmsTemplate.receive(QUEUE_A.getDestination()));
	}

	@Test
	public void postMessageWithTTLAndConsumeBeforeItExpires() throws InterruptedException {

		final int ttl = 3000;
		jmsTemplate.convertAndSend(QUEUE_A.getDestination(), "1", msg -> {
			msg.setJMSExpiration(ttl);
			return msg;
		});

		jmsTemplate.setReceiveTimeout(1000);
		final Message msg = jmsTemplate.receive(QUEUE_A.getDestination());
		Assert.assertNotNull(msg);
	}

	@Test
	public void postMessageToDLQWhenItExpires() throws InterruptedException {

		final int ttl = 3000;
		jmsTemplate.convertAndSend(QUEUE_B.getDestination(), "1", msg -> {
			msg.setJMSExpiration(ttl);
			return msg;
		});
		Thread.sleep(ttl + 1000);
		jmsTemplate.setReceiveTimeout(1000);

		// the messages its not at queue anymore
		Assert.assertNull(jmsTemplate.receive(QUEUE_B.getDestination()));

		// the messages must be in DLQ
		Assert.assertNotNull(jmsTemplate.receive(QUEUE_B.getDLQ()));
	}


}
