package com.mageddo.jms.dao;

import com.mageddo.jms.entity.DestinationParameterEntity;
import com.mageddo.jms.queue.CompleteDestination;

/**
 * Created by elvis on 21/05/17.
 */
public interface DestinationParameterDAO {

	void save(DestinationParameterEntity entity);

	void persist(CompleteDestination dest);

	DestinationParameterEntity findByName(String name);

}
