package com.mageddo.jms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mageddo.jms.entity.DestinationParameterEntity;
import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.service.activemq.ActiveMQAdmin;
import com.mageddo.jms.service.activemq.vo.QueueDetailsVO;
import com.mageddo.jms.utils.QueueUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.jms.*;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by elvis on 22/05/17.
 */

@Service
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class QueueService {

	private static final String ACTIVEMQ_CHARSET = "UTF-8";
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private DestinationParameterService destinationParameterService;

	@Autowired
	private BeanFactory beanFactory;

	@Autowired
	private ActiveMQAdmin activeMQAdmin;

	@Autowired
	private JmsTemplate jmsTemplate;

	public void changeConsumersAndSave(DestinationParameterEntity entity) {

		logger.info("status=begin, name={}", entity.getName());

		final DestinationEnum destination = DestinationEnum.fromDestinationName(entity.getName());
		Assert.notNull(destination, "destination not found: " + entity.getName());

		destinationParameterService.changeConsumers(entity.getName(), entity.getConsumers(), entity.getMaxConsumers());

		final DefaultMessageListenerContainer container = beanFactory.getBean(
			QueueUtils.getContainerName(destination.getCompleteDestination()), DefaultMessageListenerContainer.class
		);
		container.setConcurrentConsumers(entity.getConsumers());
		container.setMaxConcurrentConsumers(entity.getMaxConsumers());

		logger.info("status=success");

	}

	public List<QueueDetailsVO> getQueueDetails(){

		final List<QueueDetailsVO> queueDetailsVOs = new ArrayList<>();
		for(final DestinationEnum destinationEnum : DestinationEnum.values()){

			QueueDetailsVO vo;

			vo = toQueueDetails(destinationEnum.getDestination().getPhysicalName());
			queueDetailsVOs.add(vo);

			vo = toQueueDetails(destinationEnum.getDlq().getPhysicalName());
			queueDetailsVOs.add(vo);
		}
		return queueDetailsVOs;

	}

	public int moveAllQueueMessages(final String source, final String target){

		validateQueueName(source);
		validateQueueName(target);

		logger.info("status=begin, source={}, target={}", source, target);
		int movedMessages = 0;
		while (!isQueueEmpty(source)){
			moveNextQueueMessage(source, target);
			movedMessages++;
		}
		logger.info("status=success, movedMessages={}", movedMessages);
		return movedMessages;

	}

	public int moveQueueMessages(final String source, final String target, int limit){

		validateQueueName(source);
		validateQueueName(target);

		logger.info("status=begin, source={}, target={}, limit={}", source, target, limit);
		int movedMessages = 0;
		for (int i=0; i < limit && !isQueueEmpty(source); i++){
			moveNextQueueMessage(source, target);
			movedMessages++;
		}
		logger.info("status=success, movedMessages={}", movedMessages);
		return movedMessages;

	}


	public String getQueueMessagesAsString(String queueName, int limit){

		final List<String> queueMessages = getQueueMessages(queueName, limit);
		final StringBuilder builder = new StringBuilder();
		for(String message: queueMessages){
			builder.append(message);
			builder.append('\n');
		}
		return builder.toString();
	}

	/**
	 * Retorna todas as mensagens da fila
	 * @param queueName
	 * @return
	 */

	public List<String> getQueueMessages(String queueName){
		return getQueueMessages(queueName, getQueueSize(queueName));
	}

	public List<String> consumeQueueMessages(final String queueName){
		final List<String> messages = new ArrayList<>();
		String message;
		while((message = consumeMessageAsString(queueName)) != null){
			messages.add(message);
		}
		return messages;
	}

	/**
	 * Consome a proxima mensagem da fila como string
	 * @return a string da mensagem ou null se nao existem mais mensagens
	 */

	public String consumeMessageAsString(String queueName){
		final Message message = jmsTemplate.receive(queueName);
		if(message == null){
			return null;
		}
		final String rawMessage = convertToString(message);
		return rawMessage;
	}

	public List<String> getQueueMessages(String queueName, int limit){

		logger.info("status=begin, queueName={}, limit={}", queueName, limit);
		validateQueueName(queueName);

		final List<String> messages = new ArrayList<>();
		for(int i=0; i < limit && !isQueueEmpty(queueName); i++){
			final Message message = getMessage(queueName);
			if(message == null){
				logger.warn("status=queue-return-null");
				continue;
			}
			messages.add(convertToString(message));
		}
		logger.info("status=success, messages={}", messages.size());
		return messages;

	}

	public void purgeQueue(String destination){
		validateQueueName(destination);
		activeMQAdmin.purgeQueue(destination);
	}

	public void deleteQueue(String destination){
		validateQueueName(destination);
		activeMQAdmin.deleteQueue(destination);
	}

	public void deleteAllQueues(){

		for (final DestinationEnum destinationEnum : DestinationEnum.values()) {
			activeMQAdmin.deleteQueue(destinationEnum.getDestination().getPhysicalName());
			activeMQAdmin.deleteQueue(destinationEnum.getDlq().getPhysicalName());
		}

	}

	/**
	 * Move a proxima mensagem da source para a target
	 * @param source
	 * @param target
	 * @return true se a source ficou vazia depois de mover ou false se ainda existirem mensagens nela
	 */
	private void moveNextQueueMessage(String source, String target){

		logger.info("status=begin, source={}, target={}", source, target);
		validateQueueName(source);
		validateQueueName(target);

		if(source.equals(target)){
			throw new IllegalArgumentException("Nao pode mover para a mesma fila");
		}

		final Message message = jmsTemplate.receive(source);
		if(message == null){
			logger.warn("status=null-message");
			return ;
		}
		jmsTemplate.convertAndSend(target, message);
		logger.info("status=success");

	}

	private boolean isQueueEmpty(String source) {
		return getQueueSize(source) <= 0;
	}

	private QueueDetailsVO toQueueDetails(final String queueName){
		int queueSize = getQueueSize(queueName);
		final QueueDetailsVO queueDetailsVO = new QueueDetailsVO(queueName, queueSize);
		return queueDetailsVO;
	}

	public int getQueueSize(String queueName) {
		if(queueExists(queueName)){
			return (int) activeMQAdmin.getProperty(queueName, "QueueSize").getValue();
		}
		return -1;
	}


	private boolean queueExists(String queueName){
		return activeMQAdmin.findMBeanByDestination(queueName).getMainBean() != null;
	}

	private void validateQueueName(String queueName){
		if(!queueExists(queueName)){
			throw new IllegalArgumentException(String.format("a fila: %s nao existe", queueName));
		}
	}

	/**
	 * Recupera a proxima mensagem da fila sem apaga-la
	 * @param queueName
	 * @return
	 */
	private Message getMessage(final String queueName) {
		throw new UnsupportedOperationException();
	}


	public void postMessage(String destination, String message){
		this.jmsTemplate.convertAndSend(destination, message);
	}

	private String convertToString(Message message) {
		logger.info("msgType={}", message.getClass().getSimpleName());
		try {
			final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
			if(message instanceof ObjectMessage){
				return objectMapper.writeValueAsString(((ObjectMessage) message).getObject());
			}else if(message instanceof BytesMessage){
				return ((BytesMessage) message).readUTF();
			} else if(message instanceof TextMessage){
				return ((TextMessage) message).getText();
			}
			return null;
		}catch (Exception e){
			logger.error("msg={}", e.getMessage(), e);
			return null;
		}
	}

}
