package com.mageddo.jms.queue.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.util.MimeType;
import org.springframework.util.ObjectUtils;

import javax.jms.*;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by elvis on 28/05/17.
 */
public class DefaultMessageConverter extends SimpleMessageConverter {

	public static final String CONTENT_TYPE_KEY = "_type";
	private ObjectMapper objectMapper;
	private MessageType messageType = MessageType.BYTES;

	public DefaultMessageConverter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Object fromMessage(Message message) throws JMSException, MessageConversionException {
		if(message instanceof ObjectMessage){
			final Serializable object = ((ObjectMessage) message).getObject();
			if(object instanceof String){
				return ((String) object).getBytes();
			} else if(object instanceof byte[]){
				return object;
			}
			return object;
		} else if (message instanceof TextMessage){
			return ((TextMessage) message).getText().getBytes();
		} else if(message instanceof BytesMessage){
			byte[] bytes = new byte[(int) ((BytesMessage) message).getBodyLength()];
			((BytesMessage) message).readBytes(bytes);
			return bytes;
		}
		return super.fromMessage(message);
	}

	@Override
	public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
		if (object instanceof Message) {
			return (Message) object;
		}
		else if (object instanceof String) {
			return createMessageForByteArray(((String) object).getBytes(), session);
		}
		else if (object instanceof byte[]) {
			return createMessageForByteArray((byte[]) object, session);
		}
		try {
			final Message message;
			switch (this.messageType){
				case TEXT:
					final String json = objectMapper.writeValueAsString(object);
					message = createMessageForString(json, session);
					break;
				case BYTES:
					final byte[] jsonBytes = objectMapper.writeValueAsBytes(object);
					message = createMessageForByteArray(jsonBytes, session);
					break;
				default:
					throw new UnsupportedOperationException("Unsupported messageType: " + this.messageType);
			}
			return message;
		} catch (JsonProcessingException e) {
			throw new JMSException(String.format("class=%s, msg=%s", ObjectUtils.nullSafeClassName(object), e.getMessage()));
		}

	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
}
