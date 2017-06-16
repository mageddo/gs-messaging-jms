package com.mageddo.jms.service;

import com.mageddo.jms.dao.UserDAO;
import com.mageddo.jms.entity.UserEntity;
import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.vo.UserVO;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.DeliveryMode;
import java.util.Arrays;
import java.util.List;

/**
 * Created by elvis on 15/06/17.
 */
@Service
public class UserService {

	private static final int MAX_QUEUE_SIZE = 9999;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private QueueService queueService;

	@Autowired
	private JmsTemplate jmsTemplate;

	public void register(UserVO userVO){
		userDAO.create(new UserEntity(userVO.getName(), UserEntity.Status.PENDING));
	}

	public List<UserEntity> findNotEnqueuedRegistrations(int maxResults) {
		return userDAO.findNotEnqueuedRegistrations(maxResults);
	}

	public void markAsEnqueued(List<UserEntity> entities){
		userDAO.changeStatus(entities, UserEntity.Status.QUEUED);
	}

	public void completeRegistration(UserEntity userEntity) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		userDAO.changeStatus(Arrays.asList(userEntity), UserEntity.Status.COMPLETED);
		logger.info("userId={}", userEntity.getId());
	}

	@Transactional
	public void enqueuePendingRegistrations(){

		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		final ActiveMQDestination destination = DestinationEnum.REGISTRATION.getDestination();
		final int queueSize = queueService.getQueueSize(destination.getPhysicalName());
		if (queueSize > MAX_QUEUE_SIZE){
			logger.info("status=queue-full, queue={}", destination.getPhysicalName());
			return ;
		}

		final List<UserEntity> users = this.findNotEnqueuedRegistrations(MAX_QUEUE_SIZE - queueSize);
		for (UserEntity userEntity : users) {
			jmsTemplate.convertAndSend(destination, userEntity, msg -> {
				msg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
				return msg;
			});
		}
		this.markAsEnqueued(users);
		logger.info("status=success, time={}, enqueued={}", stopWatch.getTime(), users.size());
	}
}
