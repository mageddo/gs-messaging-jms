package com.mageddo.jms.service.activemq;

import com.mageddo.jms.service.activemq.vo.MBeanSearchVO;
import com.mageddo.jms.service.activemq.vo.PropertyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by elvis on 15/06/17.
 */
@Component
public class ActiveMQAdmin {

	private String baseURI = "http://admin:admin@activemq.dev:8161/api/jolokia";

	@Autowired
	private RestTemplate restTemplate;

	public PropertyVO getProperty(final String destination, final String property){
		final String url = String.format("%s/read/%s/%s", baseURI, findMBeanByDestination(destination).getMainBean(), property);
		return restTemplate.getForEntity(url, PropertyVO.class).getBody();
	}

	public MBeanSearchVO findMBeanByDestination(String destination){
		final String url = String.format("%s/search/*:destinationName=%s,*", baseURI, destination);
		return restTemplate.getForEntity(url, MBeanSearchVO.class).getBody();
	}

	public void deleteQueue(String destination) {
		throw new UnsupportedOperationException();
	}

	public void purgeQueue(String destination) {
		throw new UnsupportedOperationException();
	}
}
