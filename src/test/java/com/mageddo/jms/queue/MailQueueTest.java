package com.mageddo.jms.queue;

import com.mageddo.jms.ApplicationTest;
import com.mageddo.jms.SpringBootTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.Message;

/**
 * Created by elvis on 16/06/17.
 */

//@SpringBootTest
@org.springframework.boot.test.context.SpringBootTest
@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = {ApplicationTest.class}, loader = SpringBootContextLoader.class)
public class MailQueueTest {

	@Autowired
	private JmsTemplate jmsTemplate;


	@Test
	public void discardMessageWhenExpire() throws InterruptedException {
		final int ttl = 3000;
		jmsTemplate.convertAndSend("queueA", "1", msg -> {
			msg.setJMSExpiration(ttl);
			return msg;
		});
		Thread.sleep(ttl + 1000);
		final Message msg = jmsTemplate.receive("DLQ.queueA");
		Assert.assertNull(msg);
	}


}
