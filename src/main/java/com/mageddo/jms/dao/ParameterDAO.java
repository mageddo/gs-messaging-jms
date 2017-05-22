package com.mageddo.jms.dao;

import com.mageddo.jms.entity.ParameterEntity;

/**
 * Created by elvis on 21/05/17.
 */
public interface ParameterDAO {

	void save(ParameterEntity parameterEntity);

	ParameterEntity findByName(ParameterEntity.Parameter name);

}
