package com.mageddo.jms.queue;

import com.mageddo.jms.ApplicationTest;
import com.mageddo.jms.CustomLoader;
import com.mageddo.jms.queue.config.MageddoMessageListenerContainerFactory;
import com.mageddo.jms.utils.QueueUtils;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

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

	private static final String QUEUE_C_NAME = "queueC";

	private static final CompleteDestination QUEUE_C = new CompleteDestination(
		new ActiveMQQueue(QUEUE_C_NAME), 300, 3, 1,1
	);

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private PlatformTransactionManager txManager;

	@Autowired
	private ActiveMQConnectionFactory cf;

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

	@Test
	public void unsucessfullMessageNeedToBeInDLQ() throws InterruptedException {


		final CompleteDestination destination = MailQueueTest.QUEUE_C;
		QueueUtils.configureRedelivery(cf, destination);


		new TransactionTemplate(txManager, new DefaultTransactionDefinition(PROPAGATION_REQUIRES_NEW)).execute(st -> {
			jmsTemplate.convertAndSend(destination.getDestination(), "queueC");
			return null;
		});

		jmsTemplate.setReceiveTimeout(1000);

		for(int i=0; i < QUEUE_C.getRetries() + 1; i++) {

			try{
				new TransactionTemplate(txManager, new DefaultTransactionDefinition(PROPAGATION_REQUIRES_NEW)).execute(st -> {
					final Message receive = jmsTemplate.receive(destination.getDestination());
					throw new UnsupportedOperationException(receive.toString());
				});
			} catch (UnsupportedOperationException e){
				logger.info("msg={}", e.getMessage());
			}
		}

		// the messages its not at queue anymore
		Assert.assertNull(jmsTemplate.receive(destination.getDestination()));

		// the messages must be in DLQ
		Assert.assertNotNull(jmsTemplate.receive(destination.getDLQ()));
	}

}
