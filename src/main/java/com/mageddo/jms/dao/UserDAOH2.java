package com.mageddo.jms.dao;

import com.mageddo.jms.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Created by elvis on 15/06/17.
 */
@Repository
public class UserDAOH2 implements UserDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void create(UserEntity userEntity){
		jdbcTemplate.update("INSERT INTO USER (NAME, STATUS) VALUES (?, ?)", userEntity.getName(), userEntity.getStatus());
	}

}
