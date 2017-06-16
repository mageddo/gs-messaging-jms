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
		jdbcTemplate.update("INSERT INTO USER (NAME, STATUS) VALUES (?, ?)", userEntity.getName(), userEntity.getStatus().getCode());
	}

	@Override
	public List<UserEntity> findNotEnqueuedRegistrations(int maxResults) {

		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM USER  \n");
		sql.append("WHERE STATUS = ? OR (STATUS = ? AND TIMESTAMPDIFF('MINUTE', DAT_ENQUEUED, CURRENT_TIMESTAMP()) > 30) \n");
		sql.append("LIMIT 0, ?");

		return jdbcTemplate.query(
			sql.toString(), UserEntity.mapper(), Status.PENDING.getCode(), Status.QUEUED.getCode(), maxResults
		);

	}

	@Override
	public void changeStatus(List<UserEntity> entities, Status status) {

		jdbcTemplate.batchUpdate("UPDATE USER SET STATUS=? WHERE ID = ?", new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				final UserEntity userEntity = entities.get(i);
				if(userEntity.getStatus() == Status.QUEUED){
					logger.info("status=enqueue-again, user={}", userEntity.getId());
				}
				ps.setString(1, status.getCode());
				ps.setInt(2, userEntity.getId());
			}

			@Override
			public int getBatchSize() {
				return entities.size();
			}
		});
	}

}
