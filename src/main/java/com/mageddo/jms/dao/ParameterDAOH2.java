package com.mageddo.jms.dao;

import com.mageddo.jms.entity.ParameterEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by elvis on 21/05/17.
 */
@Repository
public class ParameterDAOH2 implements ParameterDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void save(ParameterEntity parameterEntity) {
		jdbcTemplate.update("UPDATE PARAMETER SET DES_VALUE=?, DAT_UPDATE=? WHERE NAM_PARAMETER=?",
			parameterEntity.getValue(), new Date(), parameterEntity.getName()
		);
	}

	@Override
	public ParameterEntity findByName(ParameterEntity.Parameter name) {
		return jdbcTemplate.queryForObject(
			"SELECT IDT_PARAMETER, NAM_PARAMETER, DES_VALUE, DAT_CREATION, DAT_UPDATE WHERE NAM_PARAMETER=?",
			ParameterEntity.class,
			new Object[]{name}
		);
	}
}
