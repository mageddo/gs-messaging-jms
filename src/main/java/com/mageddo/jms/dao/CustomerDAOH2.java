package com.mageddo.jms.dao;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Created by elvis on 21/05/17.
 */

@Repository
public class CustomerDAOH2 implements CustomerDAO {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void insert(String msg) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		jdbcTemplate.update("INSERT INTO MAIL (MESSAGE) VALUES (?)", msg);
		logger.info("status=success, time={}", stopWatch.getTime());
	}
}
