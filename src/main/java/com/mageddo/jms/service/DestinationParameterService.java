package com.mageddo.jms.service;

import com.mageddo.jms.dao.DestinationParameterDAO;
import com.mageddo.jms.entity.DestinationParameterEntity;
import com.mageddo.jms.enums.CacheNames;
import com.mageddo.jms.queue.CompleteDestination;
import com.mageddo.jms.queue.DestinationEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Created by elvis on 21/05/17.
 */
@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED	)
@CacheConfig(cacheNames = CacheNames.APP_CACHE)
@Service
public class DestinationParameterService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private DestinationParameterDAO destinationParameterDAO;

	@Cacheable(sync = true)
	public DestinationParameterEntity findByName(String name){
		return destinationParameterDAO.findByName(name);
	}

	@Cacheable(sync = true)
	public DestinationParameterEntity findByName(DestinationEnum destinationEnum){
		return findByName(destinationEnum.getCompleteDestination().getName());
	}

	public void save(DestinationParameterEntity entity){
		logger.info("status=begin, name={}", entity.getName());
		destinationParameterDAO.save(entity);
		logger.info("status=success, name={}", entity.getName());
	}

	public void changeConsumers(String name, int consumers, int maxConsumers){

		logger.info("status=begin, name={}, consumers={}, maxConsumers={}", name, consumers, maxConsumers);

		final DestinationParameterEntity destination = findByName(name);
		validateDestination(name, destination);
		destination.setConsumers(consumers);
		destination.setMaxConsumers(maxConsumers);
		this.save(destination);

		logger.info("status=success, name={}", name);
	}

	public void changeRedelivery(String name, int ttl, int maxRetries){

		logger.info("status=begin, name={}, ttl={}, maxRetries={}", name, ttl, maxRetries);

		final DestinationParameterEntity destination = findByName(name);
		validateDestination(name, destination);
		destination.setTtl(ttl);
		destination.setMaxConsumers(maxRetries);
		this.save(destination);

		logger.info("status=success, name={}", name);
	}

	private void validateDestination(String name, DestinationParameterEntity destination) {
		Assert.notNull(destination, "Could not found: " + name);
	}

	public void createDestinationParameterIfNotExists(CompleteDestination completeDestination){
		destinationParameterDAO.persist(completeDestination);
	}
}
