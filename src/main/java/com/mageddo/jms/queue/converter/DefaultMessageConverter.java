package com.mageddo.jms.queue.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.util.ObjectUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Map;

/**
 * Created by elvis on 28/05/17.
 */
public class DefaultMessageConverter extends SimpleMessageConverter {

	private ObjectMapper objectMapper;
	private MessageType messageType = MessageType.BYTES;

	public DefaultMessageConverter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
		if (object instanceof Message) {
			return (Message) object;
		}
		else if (object instanceof String) {
			return createMessageForString((String) object, session);
		}
		else if (object instanceof byte[]) {
			return createMessageForByteArray((byte[]) object, session);
		}
		else if (object instanceof Map) {
			return createMessageForMap((Map<? ,?>) object, session);
		}
		try {
			switch (this.messageType){
				case TEXT:
					final String json = objectMapper.writeValueAsString(object);
					return createMessageForString(json, session);
				case BYTES:
					final byte[] jsonBytes = objectMapper.writeValueAsBytes(object);
					return createMessageForByteArray(jsonBytes, session);
			}
		} catch (JsonProcessingException e) {
			throw new JMSException(String.format("class=%s, msg=%s", ObjectUtils.nullSafeClassName(object), e.getMessage()));
		}
		throw new MessageConversionException("Cannot convert object of type [" +
			ObjectUtils.nullSafeClassName(object) + "] to JMS message. Supported message " +
			"payloads are: String, byte array, Map<String,?>, Serializable object.");
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
}
