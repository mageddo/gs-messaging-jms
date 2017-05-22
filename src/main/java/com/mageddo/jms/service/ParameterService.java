package com.mageddo.jms.service;

import com.mageddo.jms.dao.ParameterDAO;
import com.mageddo.jms.entity.ParameterEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by elvis on 21/05/17.
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED	)
public class ParameterService {

	@Autowired
	private ParameterDAO parameterDAO;

	public ParameterEntity findByName(ParameterEntity.Parameter parameter){
		return parameterDAO.findByName(parameter);
	}
}
