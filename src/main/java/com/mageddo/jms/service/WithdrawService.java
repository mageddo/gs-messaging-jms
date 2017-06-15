package com.mageddo.jms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mageddo.jms.dao.WithdrawDAO;
import com.mageddo.jms.entity.WithdrawEntity.WithdrawStatus;
import com.mageddo.jms.entity.WithdrawEntity.WithdrawType;
import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.queue.container.BatchMessage;
import com.mageddo.jms.entity.WithdrawEntity;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import java.io.IOException;
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

	@Autowired
	private WithdrawService withdrawService;

	public void doWithdraw(BatchMessage withdraws) throws JMSException, IOException {
		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		final List<ActiveMQMessage> messages = withdraws.messages();
		for (int i = 0; i < messages.size(); i++) {

			final ActiveMQTextMessage withdrawMsg = ((ActiveMQTextMessage) messages.get(i));
			final WithdrawEntity withdrawEntity = new WithdrawEntity().parse(withdrawMsg.getText());
			long start = stopWatch.getTime();

			try{
				doWithdraw(withdrawEntity);
			} catch (UnsupportedOperationException e){
				withdrawDAO.changeStatus(withdrawEntity.getId(), WithdrawStatus.ERROR);
				withdraws.onError(withdrawMsg);
				logger.error("status=error, withdraw={}, time={}, index={}", withdrawEntity.getId(), stopWatch.getTime() - start, i);

			}

		}
		logger.info("status=success, time={}", stopWatch.getTime());
	}

	public void doWithdraw(WithdrawEntity withdrawEntity) {
		final StopWatch stopWatch = new StopWatch();
		if (withdrawEntity.getType() == WithdrawType.BANK.getType()){
			withdrawDAO.changeStatus(withdrawEntity.getId(), WithdrawStatus.COMPLETED);
			logger.info("status=consumed, withdraw={}, time={}", withdrawEntity.getId(), stopWatch.getTime());
		}else{
			throw new UnsupportedOperationException(withdrawEntity.getType() + " not supported");
		}
	}

	public void createMockWithdraw() throws JMSException {
		jmsTemplate.convertAndSend(
			DestinationEnum.WITHDRAW.getDestination(),
			new WithdrawEntity(withdrawsCounter.getAndIncrement(), WithdrawStatus.OPEN.getStatus(), WithdrawType.BANK.getType(), withdrawsCounter.get())
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
			if(new Random().nextInt(20) == 1){
				type = WithdrawType.RFID.getType();
			}else {
				type = WithdrawType.BANK.getType();
			}

			final WithdrawEntity withdraw = new WithdrawEntity(
				withdrawsCounter.getAndIncrement(), WithdrawStatus.OPEN.getStatus(), type, withdrawsCounter.get()
			);
			withdraws.add(withdraw);

		}
		this.createWithdraw(withdraws);
	}

	public void enqueuePendingWithdraws() {
		while(!withdrawService.enqueueNextPage());
	}

	/**
	 *
	 * @return true if finished
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean enqueueNextPage(){
		final List<WithdrawEntity> withdrawEntities = withdrawDAO.findWithdrawsByStatus(WithdrawStatus.OPEN);
		for (final WithdrawEntity withdrawEntity : withdrawEntities) {
			jmsTemplate.convertAndSend(DestinationEnum.WITHDRAW.getDestination(), withdrawEntity);
			withdrawDAO.changeStatus(withdrawEntity.getId(), WithdrawStatus.PROCESING);
		}
		logger.info("status=enqueued-page, withdraws={}, empty={}", withdrawEntities.size(), withdrawEntities.isEmpty());
		return withdrawEntities.isEmpty();
	}
}
