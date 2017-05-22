package com.mageddo.jms.dao;

import com.mageddo.jms.entity.DestinationParameterEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Date;

/**
 * Created by elvis on 21/05/17.
 */

@Repository
public class DestinationParameterDAOH2 implements DestinationParameterDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void save(DestinationParameterEntity entity) {

		final StringBuilder sql = new StringBuilder();
		sql.append("UPDATE DESTINATION_PARAMETER  \n");
		sql.append("	SET NUM_CONSUMERS=?, NUM_MAX_CONSUMERS=?, NUM_TTL=?, NUM_RETRIES=?, DAT_UPDATE=?  \n");
		sql.append("	WHERE NAM_DESTINATION_PARAMETER=? \n");

		entity.setUpdateDate(new Date());
		Assert.isTrue(jdbcTemplate.update(
			sql.toString(),
			entity.getConsumers(), entity.getMaxConsumers(), entity.getTtl(), entity.getRetries(), entity.getUpdateDate(),
			entity.getName()
		) == 1, "Expected one register for: " + entity.getName());
	}

	@Override
	public DestinationParameterEntity findByName(String name) {

		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT \n");
		sql.append("	IDT_DESTINATION_PARAMETER, NAM_DESTINATION_PARAMETER, NUM_CONSUMERS, \n");
		sql.append("	NUM_MAX_CONSUMERS, NUM_TTL, NUM_RETRIES, DAT_CREATION, DAT_UPDATE  \n");
		sql.append("FROM DESTINATION_PARAMETER  \n");
		sql.append("WHERE NAM_DESTINATION_PARAMETER = ? \n");

		return jdbcTemplate.queryForObject(sql.toString(), DestinationParameterEntity.rowMapper(), name);
	}
}
