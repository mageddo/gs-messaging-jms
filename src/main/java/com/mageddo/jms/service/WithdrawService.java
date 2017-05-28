package com.mageddo.jms.service;

import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.queue.container.BatchMessage;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by elvis on 28/05/17.
 */
@Service
public class WithdrawService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final AtomicInteger withdrawsCounter = new AtomicInteger(1);

	@Autowired
	private JmsTemplate jmsTemplate;

	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public void doWithdraw(BatchMessage withdraws) throws JMSException {
		for (final ActiveMQMessage withdrawMsg: withdraws.messages()) {

			final boolean success = true;//new Random().nextBoolean();
			if (success){
				logger.info("status=withdraw, msg={}", (((ActiveMQTextMessage)withdrawMsg).getText()));
			} else {
				withdraws.onError(withdrawMsg);
			}

		}
	}

	public void createMockWithdraw() throws JMSException {
		final ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setText(String.valueOf(withdrawsCounter.getAndIncrement()));
		jmsTemplate.convertAndSend(DestinationEnum.WITHDRAW.getDestination(), message);
	}
}
