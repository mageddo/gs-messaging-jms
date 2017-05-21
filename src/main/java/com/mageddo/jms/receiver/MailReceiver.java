package com.mageddo.jms.receiver;

import com.mageddo.jms.queue.QueueConstants;
import com.mageddo.jms.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Component
public class MailReceiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailReceiver.class);

	@Autowired
	private MailService mailService;

//	@Scheduled(fixedRate = 1)
	public void postMail() {
		mailService.sendMockMail();
	}

	@JmsListener(destination = QueueConstants.MAIL, containerFactory = QueueConstants.MAIL + "Factory")
	@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
	public void consume(String email) throws InterruptedException {

		mailService.insert(email);
		boolean error = false;
		if (!error) {
			LOGGER.info("status=mail-received, status=success, mail={}", email);
		} else {
			LOGGER.error("status=mail-received, status=error, mail={}", email);
			throw new RuntimeException("failed");
		}
	}



}
