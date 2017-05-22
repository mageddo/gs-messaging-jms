package com.mageddo.jms.dao;

import com.mageddo.jms.entity.DestinationParameterEntity;
import com.mageddo.jms.queue.CompleteDestination;
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
	public void persist(CompleteDestination dest ){

		final StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO DESTINATION_PARAMETER \n");
		sql.append("( \n");
		sql.append("   NAM_DESTINATION_PARAMETER,NUM_CONSUMERS,NUM_MAX_CONSUMERS,NUM_TTL,NUM_RETRIES,DAT_CREATION,DAT_UPDATE \n");
		sql.append(") \n");
		sql.append("SELECT \n");
		sql.append("* \n");
		sql.append("FROM ( SELECT \n");
		sql.append("'%s' NAM_DESTINATION_PARAMETER, \n");
		sql.append("%d NUM_CONSUMERS, \n");
		sql.append("%d NUM_MAX_CONSUMERS, \n");
		sql.append("%d NUM_TTL, \n");
		sql.append("%d NUM_RETRIES, \n");
		sql.append("'%6$tY-%6$tm-%6$td' DAT_CREATION, \n");
		sql.append("'%7$tY-%7$tm-%7$td' DAT_UPDATE \n");
		sql.append(") X \n");
		sql.append("WHERE NOT EXISTS \n");
		sql.append("( \n");
		sql.append("   SELECT 1 \n");
		sql.append("   FROM DESTINATION_PARAMETER \n");
		sql.append("   WHERE NAM_DESTINATION_PARAMETER = ? \n");
		sql.append(") \n");


		jdbcTemplate.update(String.format(sql.toString(), dest.getName(), dest.getConsumers(), dest.getMaxConsumers(),
			dest.getTTL(), dest.getRetries(), new Date(), new Date()), dest.getName()
		);
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
