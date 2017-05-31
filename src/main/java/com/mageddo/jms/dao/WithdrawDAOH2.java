package com.mageddo.jms.dao;

import com.mageddo.jms.entity.WithdrawEntity;
import com.mageddo.jms.entity.WithdrawEntity.WithdrawStatus;
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
		sql.append("	(IDT_WITHDRAW, IND_STATUS, IND_TYPE, NUM_VALUE, DAT_CREATION, DAT_UPDATE) VALUES  \n");
		sql.append("	(?, ?, ?, ?, ?, ?) \n");
		jdbcTemplate.batchUpdate(sql.toString(), new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				final WithdrawEntity withdrawEntity = withdraws.get(i);
				int index = 1;
				ps.setInt(index++, withdrawEntity.getId());
				ps.setString(index++, String.valueOf(withdrawEntity.getStatus()));
				ps.setString(index++, String.valueOf(withdrawEntity.getType()));
				ps.setDouble(index++, withdrawEntity.getValue());
				ps.setTimestamp(index++, new Timestamp(withdrawEntity.getCreationDate().getTime()));
				ps.setTimestamp(index++, new Timestamp(withdrawEntity.getUpdateDate().getTime()));
			}

			@Override
			public int getBatchSize() {
				return withdraws.size();
			}
		});
	}

	@Override
	public List<WithdrawEntity> findWithdrawsByStatus(WithdrawStatus withdrawStatus){

		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT  \n");
		sql.append("	IDT_WITHDRAW, IND_TYPE, \n");
		sql.append("	IND_STATUS, NUM_VALUE, \n");
		sql.append("	DAT_CREATION, DAT_UPDATE \n");
		sql.append("FROM WITHDRAW \n");
		sql.append("WHERE IND_STATUS = ? \n");
		sql.append("LIMIT 0, 1000 \n");

		return jdbcTemplate.query(sql.toString(), (row, i) -> {
			final WithdrawEntity w = new WithdrawEntity(
				row.getInt("IDT_WITHDRAW"),
				row.getString("IND_STATUS").charAt(0),
				row.getString("IND_TYPE").charAt(0),
				row.getDouble("NUM_VALUE")
			);
			w.setCreationDate(row.getDate("DAT_CREATION"));
			w.setUpdateDate(row.getDate("DAT_UPDATE"));
			return w;
		}, String.valueOf(withdrawStatus.getStatus()));
	}
}
