package com.mageddo.jms.service;

import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.vo.Sale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by elvis on 23/05/17.
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class SaleService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final AtomicInteger id = new AtomicInteger();

	@Autowired
	private JmsTemplate jmsTemplate;

	public void completeSale(Sale sale){
		logger.info("status=completeSale, sale={}", sale);
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {}
	}

	public void createMockSale(){
//		for(int i=0; i < 1000; i++)
		final String sale = String.format("safe %05d", id.incrementAndGet());
		logger.info("status=new-sale, sale={}", sale);
		jmsTemplate.convertAndSend(DestinationEnum.SALE.getDestination(), sale);
	}
}
