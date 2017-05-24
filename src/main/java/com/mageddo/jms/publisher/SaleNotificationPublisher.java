package com.mageddo.jms.publisher;

import com.mageddo.jms.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by elvis on 23/05/17.
 */
@Component
public class SaleNotificationPublisher {

	@Autowired
	private SaleService saleService;

//	@Scheduled(fixedRate = Integer.MAX_VALUE)
	public void postSales(){
		for(;;)
			saleService.createMockSale();
	}
}
