package com.mageddo.jms.service;

import com.mageddo.jms.dao.CustomerDAO;
import com.mageddo.jms.queue.QueueEnum;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by elvis on 21/05/17.
 */

@Service
@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
public class MailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private CustomerDAO customerDAO;

	private AtomicLong id = new AtomicLong(0);

	public void sendMail(String message){
		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		jmsTemplate.convertAndSend(QueueEnum.MAIL.getQueue(), message);
		LOGGER.info("status=success, msg={}, time={}", message, stopWatch.getTime());
	}

	public void sendMockMail(){
		sendMail(String.format("Hello %05d", id.incrementAndGet()));
	}

	public void insert(String message){
		customerDAO.insert(message);
	}
}
