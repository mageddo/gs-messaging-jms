package com.mageddo.jms.receiver;

import com.mageddo.jms.queue.DestinationConstants;
import com.mageddo.jms.service.MailService;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Random;

@Component
public class MailReceiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailReceiver.class);

	@Autowired
	private MailService mailService;

//	@Scheduled(fixedDelay = Long.MAX_VALUE)
	public void postMail() {
		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		int qtd = 1;
		mailService.sendMockMail(qtd);
		LOGGER.info("status=success, qtd={}, time={}", qtd, stopWatch.getTime());
	}

	int counter = 0;

	@JmsListener(destination = DestinationConstants.MAIL, containerFactory = "#{queue.get('MAIL').getFactory()}")
	public void consume(TextMessage email) throws InterruptedException, JMSException {

		mailService.insert(email.getText());
		boolean error = true ; // new Random().nextBoolean();
		if (!error) {
			LOGGER.info("status=success, mail={}", email);
		} else {
			LOGGER.error("status=error, mail={}, counter={}", email, ++counter);
			throw new RuntimeException("failed");
		}
	}


}
