package com.mageddo.jms.dao;

import com.mageddo.jms.entity.DestinationParameterEntity;

/**
 * Created by elvis on 21/05/17.
 */
public interface DestinationParameterDAO {

	void save(DestinationParameterEntity entity);

	DestinationParameterEntity findByName(String name);

}
