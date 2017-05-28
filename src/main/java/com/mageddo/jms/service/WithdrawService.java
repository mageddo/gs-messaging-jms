package com.mageddo.jms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by elvis on 28/05/17.
 */
@Service
public class WithdrawService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public void doWithdraw(String withdraw) {
		logger.info("status=onMessage, status=withdraw, msg={}", withdraw);
	}
}
