package com.mageddo.jms.service.activemq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mageddo.jms.service.activemq.vo.MBeanSearchVO;
import com.mageddo.jms.service.activemq.vo.PropertyVO;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Created by elvis on 15/06/17.
 */
@Component
public class ActiveMQAdmin {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private String baseURI = "http://activemq.dev:8161/api/jolokia";
	private String username = "admin";
	private String password = "admin";
	private HttpHeaders headers;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RestTemplate restTemplate;

	@PostConstruct
	public void construct(){
		final String base64Creds = new String(Base64.encodeBase64(String.format("%s:%s", username, password).getBytes()));
		headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
	}

	public PropertyVO getProperty(final String destination, final String property){
		final String url = String.format("%s/read/%s/%s", baseURI, findMBeanByDestination(destination).getMainBean(), property);
		return doRequest(url, HttpMethod.GET, PropertyVO.class);
	}

	public MBeanSearchVO findMBeanByDestination(String destination){
		final String url = String.format(
			"%s/search/org.apache.activemq:destinationName=%s,destinationType=Queue,type=Broker,brokerName=*",
			baseURI, destination
		);
		return doRequest(url, HttpMethod.GET, MBeanSearchVO.class);
	}

	public void deleteQueue(String destination) {
		throw new UnsupportedOperationException();
	}

	public void purgeQueue(String destination) {
		throw new UnsupportedOperationException();
	}

	private <T>T doRequest(String url, HttpMethod method, Class<T> clazz) {
		final String body = restTemplate.exchange(url, method, new HttpEntity<>(headers), String.class).getBody();
		try {
			return objectMapper.readValue(body, clazz);
		} catch (IOException e) {
			logger.error("msg={}", e.getMessage(), e);
			return null;
		}
	}
}
