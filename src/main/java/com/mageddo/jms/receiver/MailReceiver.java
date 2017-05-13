package com.mageddo.jms.receiver;

import com.mageddo.jms.Email;
import com.mageddo.jms.queue.QueueConstants;
import com.mageddo.jms.queue.QueueEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.Scanner;

@Component
public class MailReceiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailReceiver.class);

	@Autowired
	private JmsTemplate jmsTemplate;

	private int id = 0;

	@Scheduled(fixedDelay = 500)
	public void postMail() {
		jmsTemplate.convertAndSend(QueueEnum.MAIL.getQueue(), String.format("Hello %04d", ++id));
	}

	@JmsListener(destination = QueueConstants.MAIL, containerFactory = QueueConstants.MAIL + "Factory")
	public void consume(String email) throws InterruptedException {

//		if (new Random().nextInt(30) == 3) {
		LOGGER.info("status=mail-received, status=begin, mail={}", email);
		boolean error = true;
		if (!error) {
			Thread.sleep(250);
			LOGGER.info("status=mail-received, status=success, mail={}", email);
		} else {
			LOGGER.error("status=mail-received, status=error, mail={}", email);
			throw new RuntimeException("failed");
		}
	}



}
