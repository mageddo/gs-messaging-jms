package com.mageddo.jms.receiver;

import com.mageddo.jms.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MailReceiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailReceiver.class);

	@Autowired
	private JmsTemplate jmsTemplate;

//	@Scheduled(fixedDelay = 500)
	public void postMail() {

		final Email email = new Email("info@example.com", "Hello");
		LOGGER.info("status=mail-post, to={}, msg={}", email.getTo(), email.getBody());
		jmsTemplate.convertAndSend("mailbox", email);
	}

	@JmsListener(destination = "mailbox", containerFactory = "mailContainer")
	public void receiveMessage(Email email) throws InterruptedException {

		Thread.sleep(250);

		LOGGER.info("status=mail-received, email={}", email);
	}



}
