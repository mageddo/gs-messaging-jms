package com.mageddo.jms.queue.converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by elvis on 28/05/17.
 */
public class JsonMessageConverter extends MappingJackson2MessageConverter {

	private ObjectMapper objectMapper;
	private MessageType targetType = MessageType.BYTES;

	public JsonMessageConverter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		super.setObjectMapper(objectMapper);
	}

	@Override
	public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {

		if (object instanceof Message) {

			try {
				final Message message = this.mapToMessage(object, session, objectMapper.writer(), this.targetType);
				return message;
			} catch (IOException ex) {
				throw new MessageConversionException("Could not map JSON object [" + object + "]", ex);
			}
		}

		return super.toMessage(object, session);
	}

	protected Message mapToMessage(Object object, Session session, ObjectWriter objectWriter, MessageType targetType)
		throws JMSException, IOException {

		if(object instanceof ActiveMQObjectMessage){

			final StringWriter writer = new StringWriter();
			final ActiveMQObjectMessage objectMessage = (ActiveMQObjectMessage) object;

			setTypeIdOnMessage(objectMessage.getObject(), objectMessage);

			objectWriter.writeValue(writer, objectMessage.getObject());
			objectMessage.setObject(writer.toString());

			return objectMessage;

		}
		return super.mapToMessage(object, session, objectWriter, targetType);
	}

	@Override
	protected Object convertFromMessage(Message message, JavaType targetJavaType) throws JMSException, IOException {
		if (message instanceof ObjectMessage) {
			return ((ObjectMessage) message).getObject();
		}
		return super.convertFromMessage(message, targetJavaType);
	}

	@Override
	public void setObjectMapper(ObjectMapper objectMapper) {
		super.setObjectMapper(objectMapper);
		this.objectMapper = objectMapper;
	}

	@Override
	public void setTargetType(MessageType targetType) {
		super.setTargetType(targetType);
		this.targetType = targetType;
	}
}