package com.mageddo.jms.service;

import com.mageddo.jms.dao.CustomerDAO;
import com.mageddo.jms.queue.DestinationEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.DeliveryMode;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by elvis on 21/05/17.
 */

@Service
@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
public class MailService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private CustomerDAO customerDAO;

	private AtomicLong id = new AtomicLong(0);

	public void sendMail(String message){
		jmsTemplate.convertAndSend(DestinationEnum.MAIL.getDestination(), message, msg -> {
//			msg.setJMSExpiration(180 * 1000);
//			msg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
			return msg;
		});
	}

	public void sendMockMail(int qtd){
		for(int i =0; i < qtd; i++){
			final String message = String.format("%05d", id.incrementAndGet());
			sendMail(message);
		}
	}

	public String sendMockMail(){
		final String message = String.format("%05d", id.incrementAndGet());
		sendMail(message);
		return message;
	}

	public void insert(String message){
		customerDAO.insert(message);
	}
}
