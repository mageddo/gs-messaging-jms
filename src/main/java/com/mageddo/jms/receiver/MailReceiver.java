package com.mageddo.jms.receiver;

import com.mageddo.jms.Email;
import com.mageddo.jms.queue.QueueConstants;
import com.mageddo.jms.queue.QueueEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.Scanner;

@Component
public class MailReceiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailReceiver.class);

	@Autowired
	private JmsTemplate jmsTemplate;

//	@Scheduled(fixedDelay = 500)
	public void postMail() {

		final Email email = new Email("info@example.com", "Hello");
		LOGGER.info("status=mail-post, to={}, msg={}", email.getTo(), email.getBody());
		jmsTemplate.convertAndSend(QueueEnum.MAIL.getQueue(), email);
	}

	@JmsListener(destination = QueueConstants.MAIL, containerFactory = QueueConstants.MAIL + "Factory")
	public void consume(String email) throws InterruptedException {

		LOGGER.info("status=mail-received, mail={}, status=begin", email);
//		if (new Random().nextInt(30) == 3) {
		boolean error = true;
		if (!error) {
			Thread.sleep(250);
			LOGGER.info("status=mail-received, mail={}, status=success", email);
		} else {
			LOGGER.error("status=mail-received, mail={}, status=error", email);
			throw new RuntimeException("failed");
		}
	}



}
