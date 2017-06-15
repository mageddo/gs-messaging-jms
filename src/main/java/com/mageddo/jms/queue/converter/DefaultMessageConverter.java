package com.mageddo.jms.queue.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.command.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.util.MimeType;
import org.springframework.util.ObjectUtils;

import javax.jms.*;
import javax.jms.Message;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Random;

/**
 * Created by elvis on 28/05/17.
 */
public class DefaultMessageConverter extends SimpleMessageConverter {

	private ObjectMapper objectMapper;

	public DefaultMessageConverter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Object fromMessage(Message message) throws JMSException, MessageConversionException {
		return message;
	}

	@Override
	public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {

		if (object instanceof Message) {
			return (Message) object;
		}
		else if (object instanceof String) {
			return createMessageForString(((String) object), session);
		}
		else if (object instanceof byte[]) {
			return createMessageForByteArray((byte[]) object, session);
		}
		try {
			return createMessageForString(objectMapper.writeValueAsString(object), session);
		} catch (JsonProcessingException e) {
			throw new JMSException(e.getMessage());
		}

	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
}
