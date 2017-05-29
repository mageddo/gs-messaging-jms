package com.mageddo.jms.service;

import com.mageddo.jms.dao.WithdrawDAO;
import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.queue.container.BatchMessage;
import com.mageddo.jms.entity.WithdrawEntity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by elvis on 28/05/17.
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class WithdrawService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final AtomicInteger withdrawsCounter = new AtomicInteger(1);

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private WithdrawDAO withdrawDAO;

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
		jmsTemplate.convertAndSend(
			DestinationEnum.WITHDRAW.getDestination(),
			new WithdrawEntity(withdrawsCounter.getAndIncrement(), WithdrawEntity.WithdrawType.BANK.getType(), withdrawsCounter.get())
		);
	}

	public void createWithdraw(List<WithdrawEntity> withdraws){
		withdrawDAO.createWithdraw(withdraws);
	}

	public void createMockWithdraws(int batchSize) {
		logger.info("batchSize={}", batchSize);
		final List<WithdrawEntity> withdraws = new ArrayList<>();
		for (int j=0; j < batchSize; j++){

			final char type;
			if(new Random().nextInt(10) == 1){
				type = WithdrawEntity.WithdrawType.RFID.getType();
			}else {
				type = WithdrawEntity.WithdrawType.BANK.getType();
			}

			final WithdrawEntity withdraw = new WithdrawEntity(
				withdrawsCounter.getAndIncrement(), type, withdrawsCounter.get()
			);
			withdraws.add(withdraw);

		}
		this.createWithdraw(withdraws);
	}
}
