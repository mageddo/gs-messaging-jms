package com.mageddo.jms.service;

import com.mageddo.jms.entity.DestinationParameterEntity;
import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.utils.QueueUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Created by elvis on 22/05/17.
 */

@Service
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class QueueService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private DestinationParameterService destinationParameterService;

	@Autowired
	private BeanFactory beanFactory;

	public void changeConsumersAndSave(DestinationParameterEntity entity) {

		logger.info("status=begin, name={}", entity.getName());

		final DestinationEnum destination = DestinationEnum.fromDestinationName(entity.getName());
		Assert.notNull(destination, "destination not found: " + entity.getName());

		destinationParameterService.changeConsumers(entity.getName(), entity.getConsumers(), entity.getMaxConsumers());

		final DefaultMessageListenerContainer container = beanFactory.getBean(
			QueueUtils.getContainerName(destination.getCompleteDestination()), DefaultMessageListenerContainer.class
		);
		container.setConcurrentConsumers(entity.getConsumers());
		container.setMaxConcurrentConsumers(entity.getMaxConsumers());

		logger.info("status=success");

	}

}
