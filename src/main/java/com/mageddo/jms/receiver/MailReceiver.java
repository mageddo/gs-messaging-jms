package com.mageddo.jms.receiver;

import com.mageddo.jms.queue.QueueConstants;
import com.mageddo.jms.queue.QueueEnum;
import org.apache.commons.lang3.time.StopWatch;
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

	private int id = 0;

	@Scheduled(fixedRate = 1)
	public void postMail() {
//		for(;;) {
			final StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			final String msg = String.format("Hello %05d", ++id);
			jmsTemplate.convertAndSend(QueueEnum.MAIL.getQueue(), msg);
			LOGGER.info("status=success, msg={}, time={}", msg, stopWatch.getTime());
//		}
	}

//	@JmsListener(destination = QueueConstants.MAIL, containerFactory = QueueConstants.MAIL + "Factory")
	public void consume(String email) throws InterruptedException {

//		if (new Random().nextInt(30) == 3) {
//		LOGGER.info("status=mail-received, status=begin, mail={}", email);
		boolean error = false;
		if (!error) {
			Thread.sleep(50);
			LOGGER.info("status=mail-received, status=success, mail={}", email);
		} else {
			LOGGER.error("status=mail-received, status=error, mail={}", email);
			throw new RuntimeException("failed");
		}
	}



}
