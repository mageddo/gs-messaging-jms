package com.mageddo.jms.service;

import com.mageddo.jms.vo.Sale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by elvis on 23/05/17.
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class SaleService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public void completeSale(Sale sale){
		logger.info("status=completeSale, sale={}", sale);
	}
}
