package com.mageddo.jms.jmx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mageddo.jms.entity.DestinationParameterEntity;
import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.service.QueueService;
import com.mageddo.jms.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Component
@ManagedResource
public class QueueUtilJMX {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private QueueService queueService;

	@ManagedOperation
	public void changeConsumers(String name, int consumers, int maxConsumers){

		final DestinationParameterEntity entity = new DestinationParameterEntity();
		entity.setName(name);
		entity.setConsumers(consumers);
		entity.setMaxConsumers(maxConsumers);

		queueService.changeConsumersAndSave(entity);
	}

	@ManagedOperation(description = "Move o conteudo de uma fila para outra")
	@ManagedOperationParameters({ //
		@ManagedOperationParameter(name = "source", description = "A fila de origem, ex: ha_at_myqueue, ha_at_myqueueDLQ, etc."),
		@ManagedOperationParameter(name = "target", description = "A fila de destino, ex: mesmo formato do anterior")
	})
	public String moveAllQueueMessages(String source, String target){

		try{
			logger.info("M=moveAllQueueMessages, status=start, source={}, target={}", source, target);
			int moved = queueService.moveAllQueueMessages(source, target);
			logger.info("M=moveAllQueueMessages, status=success");
			return "success: " + moved + " messages moved";
		}catch (Exception e){
			logger.error("M=moveAllQueueMessages, msg={}", e.getMessage(), e);
			return "error: " + e.toString();
		}

	}

	@ManagedOperation(description = "Move a quantidade especificada de mensagens de uma fila para outra")
	@ManagedOperationParameters({ //
		@ManagedOperationParameter(name = "source", description = "A fila de origem, ex: ha_at_myqueue, ha_at_myqueueDLQ, etc."),
		@ManagedOperationParameter(name = "target", description = "A fila de destino, ex: mesmo formato do anterior"),
		@ManagedOperationParameter(name = "quantity", description = "Quantidade de mensagens a serem movidas")
	})
	public String moveQueueMessages(String source, String target, int quantity){

		try{
			logger.info("M=moveQueueMessages, status=start, source={}, target={}, quantity={}", source, target, quantity);
			int moved = queueService.moveQueueMessages(source, target, quantity);
			logger.info("M=moveQueueMessages, status=success");
			return "success: " + moved + " messages moved";
		}catch (Exception e){
			logger.error("M=moveQueueMessages, msg={}", e.getMessage(), e);
			return "error: " + e.toString();
		}

	}

	@ManagedOperation(description = "Move o conteudo de uma fila para DLQ")
	@ManagedOperationParameters({ //
		@ManagedOperationParameter(name = "queueEnum", description = "A constante do enum da queue, ex: MY_QUEUE")
	})
	public String moveQueueToDLQ(final String queueEnum) {
		try {
			try {
				logger.info("M=moveQueueToDLQ, status=start, queueEnum={}", queueEnum);
				final DestinationEnum queueDesc = DestinationEnum.valueOf(queueEnum);
				final int moved = queueService.moveAllQueueMessages(
					queueDesc.getDestination().getPhysicalName(),
					queueDesc.getDlq().getPhysicalName()
				);
				logger.info("M=moveQueueToDLQ, status=success");
				return "success: " + moved + " messages moved";
			}catch(IllegalArgumentException e){
				return "warning: fila nao existe:" + queueEnum;
			}
		}catch (Exception e){
			logger.error("M=moveQueueToDLQ, msg={}", e.getMessage(), e);
			return "error: " + e.toString();
		}
	}

	@ManagedOperation(description = "Recupera o conteudo da DLQ")
	@ManagedOperationParameters({ //
		@ManagedOperationParameter(name = "queueEnum", description = "A constante da queue, ex: MY_QUEUE")
	})
	public String recoverQueueFromDLQ(final String queueEnum){
		try {
			try {
				logger.info("M=recoverQueueFromDLQ, status=start, queueEnum={}", queueEnum);
				final DestinationEnum queueDesc = DestinationEnum.valueOf(queueEnum);
				final int moved = queueService.moveAllQueueMessages(
					queueDesc.getDlq().getPhysicalName(), queueDesc.getDestination().getPhysicalName()
				);
				logger.info("M=recoverQueueFromDLQ, status=success");
				return "success: " + moved + " messages moved";
			}catch(IllegalArgumentException e){
				return "warning: fila nao existe:" + queueEnum;
			}
		}catch (Exception e){
			logger.error("M=recoverQueueFromDLQ, msg={}", e.getMessage(), e);
			return "error: " + e.toString();
		}
	}

	@ManagedOperation(description = "Pega o conteudo das mensagens da fila, para limpar a mensagem de retorno do hawtio leia o README.md")
	@ManagedOperationParameters({ //
		@ManagedOperationParameter(name = "queueName", description = "O nome da fila, ex: ha_at_my_queue"),
		@ManagedOperationParameter(name = "messagesQtd", description = "quantidade de mensagens a recuperar")
	})
	public String getQueueMessagesContent(final String queueName, int messagesQtd){
		try {
			logger.info("M=getQueueMessagesContent, status=start, queueName={}", queueName);
			final String queueMessages = queueService.getQueueMessagesAsString(queueName, messagesQtd);
			logger.info("M=getQueueMessagesContent, status=success");
			return queueMessages;
		}catch (Exception e){
			logger.error("M=getQueueMessagesContent, msg={}", e.getMessage(), e);
			return "error: " + e.toString();
		}
	}

	@ManagedOperation(description = "Apaga todas as mensagens da fila")
	@ManagedOperationParameters({ //
		@ManagedOperationParameter(name = "queueName", description = "O nome da fila, ex: ha_at_my_queue")
	})
	public String purgeQueue(final String queueName){
		try {
			logger.info("M=purgeQueue, status=start, queueName={}", queueName);
			queueService.purgeQueue(queueName);
			logger.info("M=purgeQueue, status=success");
			return "success";
		}catch (Exception e){
			logger.error("M=purgeQueue, msg={}", e.getMessage(), e);
			return "error: " + e.toString();
		}
	}

	@ManagedOperation(description = "Deleta a fila")
	@ManagedOperationParameters({ //
		@ManagedOperationParameter(name = "queue", description = "Nome da fila, ex: ha_at_myqueue, ha_at_myqueueDLQ, etc.")
	})
	public String deleteQueue(String queue){

		try{
			logger.info("M=deleteQueue, status=start, source={}", queue);
			queueService.deleteQueue(queue);
			logger.info("M=deleteQueue, status=success");
			return "success: "+ queue +" deleted";
		}catch (Exception e){
			logger.error("M=deleteQueue, msg={}", e.getMessage(), e);
			return "error: " + e.toString();
		}

	}

	@ManagedOperation(description = "Deleta todas as filas deste projeto")
	public String deleteAllQueues(){
		try{
			logger.info("M=deleteAllQueues, status=start");
			queueService.deleteAllQueues();
			logger.info("M=deleteAllQueues, status=success");
			return "success";
		}catch (Exception e){
			logger.error("M=deleteAllQueues, msg={}", e.getMessage(), e);
			return "error: " + e.toString();
		}

	}

	@ManagedOperation(description = "Mostra o detalhe de todas as filas")
	public String queueDetails(){
		try{
			logger.info("M=queueDetails, status=start");
			final ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			return objectMapper.writeValueAsString(queueService.getQueueDetails());
		}catch (Exception e){
			logger.error("M=queueDetails, msg={}", e.getMessage(), e);
			return "error: " + e.toString();
		}
	}

	@ManagedOperation(description = "Posta uma mensagem")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "destination", description = "The destination name"),
		@ManagedOperationParameter(name = "msg", description = "O conteudo da mensagem")
	})
	public String postMessage(String destination, String msg){
		try{
			logger.info("M=postMessage, status=start");
			queueService.postMessage(destination, msg);
			return "success";
		}catch (Exception e){
			logger.error("M=queueDetails, msg={}", e.getMessage(), e);
			return "error: " + e.toString();
		}
	}

	@ManagedOperation(description = "Gets queue size")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "destination", description = "The destination name")
	})
	public String getQueueSize(String destination){
		try{
			logger.info("status=start, destination={}", destination);
			return String.valueOf(queueService.getQueueSize(destination));
		}catch (Exception e){
			logger.error("msg={}", e.getMessage(), e);
			return "error: " + e.toString();
		}
	}

	@ExceptionHandler
	public @ResponseBody ResponseEntity<PropertiesUtils.InnerProperties> exception(Throwable e){
		logger.error("status=begin, msg={}", e.getMessage(), e);
		return ResponseEntity
			.badRequest()
			.body(PropertiesUtils.prop().put("msg", e.getMessage()));
	}
}