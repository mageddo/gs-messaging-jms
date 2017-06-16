package com.mageddo.jms.dao;

import com.mageddo.jms.entity.UserEntity;
import com.mageddo.jms.entity.UserEntity.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Created by elvis on 15/06/17.
 */
@Repository
public class UserDAOH2 implements UserDAO {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void create(UserEntity userEntity){
		jdbcTemplate.update(
			"INSERT INTO USER (NAM_USER, IND_STATUS) VALUES (?, ?)",
			userEntity.getName(), userEntity.getStatus().getCode()
		);
	}

	@Override
	public List<UserEntity> findNotEnqueuedRegistrations(int maxResults) {

		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM USER  \n");
		sql.append("WHERE IND_STATUS = ? OR (IND_STATUS = ? AND TIMESTAMPDIFF('MINUTE', DAT_ENQUEUED, CURRENT_TIMESTAMP()) > 5) \n");
		sql.append("LIMIT 0, ?");

		return jdbcTemplate.query(
			sql.toString(), UserEntity.mapper(), Status.PENDING.getCode(), Status.QUEUED.getCode(), maxResults
		);

	}

	@Override
	public void changeStatus(List<UserEntity> entities, Status status) {

		jdbcTemplate.batchUpdate("UPDATE USER SET IND_STATUS=? WHERE IDT_USER = ?", new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				final UserEntity userEntity = entities.get(i);
				ps.setString(1, status.getCode());
				ps.setInt(2, userEntity.getId());
			}

			@Override
			public int getBatchSize() {
				return entities.size();
			}
		});
	}

	@Override
	public void enqueue(List<UserEntity> entities) {

		jdbcTemplate.batchUpdate("UPDATE USER SET IND_STATUS=?, DAT_ENQUEUED=? WHERE IDT_USER = ?", new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				final UserEntity userEntity = entities.get(i);
				if(userEntity.getStatus() == Status.QUEUED){
					logger.info("status=enqueue-again, user={}", userEntity.getId());
				}
				ps.setString(1, Status.QUEUED.getCode());
				ps.setTimestamp(2, new Timestamp(new Date().getTime()));
				ps.setInt(3, userEntity.getId());
			}

			@Override
			public int getBatchSize() {
				return entities.size();
			}
		});
	}

}
