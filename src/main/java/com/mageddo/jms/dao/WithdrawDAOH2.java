package com.mageddo.jms.dao;

import com.mageddo.jms.entity.WithdrawEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by elvis on 29/05/17.
 */
@Repository
public class WithdrawDAOH2 implements WithdrawDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void createWithdraw(final List<WithdrawEntity> withdraws){

		final StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO WITHDRAW  \n");
		sql.append("	(IDT_WITHDRAW, IND_TYPE, NUM_VALUE, DAT_CREATION, DAT_UPDATE) VALUES  \n");
		sql.append("	(?, ?, ?, ?, ?) \n");
		jdbcTemplate.batchUpdate(sql.toString(), new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				final WithdrawEntity withdrawEntity = withdraws.get(i);
				ps.setInt(1, withdrawEntity.getId());
				ps.setString(2, String.valueOf(withdrawEntity.getType()));
				ps.setDouble(3, withdrawEntity.getValue());
				ps.setTimestamp(4, new Timestamp(withdrawEntity.getCreationDate().getTime()));
				ps.setTimestamp(5, new Timestamp(withdrawEntity.getUpdateDate().getTime()));
			}

			@Override
			public int getBatchSize() {
				return withdraws.size();
			}
		});
	}
}
