package com.mageddo.jms.queue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by elvis on 15/06/17.
 */

@Component
public class JsonConverter {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	ObjectMapper objectMapper;

	public <T>T readValue(Message message, Class<T> clazz) throws JMSException {
		return readValue(message, (Object)clazz);
	}
	public <T>T readValue(Message message, TypeReference clazz) throws JMSException {
		return readValue(message, (Object)clazz);
	}

	private <T>T readValue(Message message, Object type) throws JMSException {

		TypeReference<T> typeReference = null;
		Class<T> clazz = null;
		if(type instanceof TypeReference){
			typeReference = (TypeReference) type;
		}else {
			clazz = (Class) type;
		}

		try{
			if(message instanceof BytesMessage){
				final InputStream in = new InputStream() {
					@Override
					public int read() throws IOException {
						try {
							return ((BytesMessage) message).readByte();
						} catch (JMSException e) {
							throw new IOException(e);
						}
					}
				};
				if(typeReference != null){
					return objectMapper.readValue(in, typeReference);
				}
				return objectMapper.readValue(in, clazz);
			}else if(message instanceof TextMessage){
				if(typeReference != null){
					return objectMapper.readValue(((TextMessage) message).getText(), typeReference);
				}
				return objectMapper.readValue(((TextMessage) message).getText(), clazz);
			} else if(message instanceof ObjectMessage){
				final Serializable object = ((ObjectMessage) message).getObject();
				if(object instanceof String){
					if(typeReference != null){
						return objectMapper.readValue((String) object, typeReference);
					}
					return objectMapper.readValue((String) object, clazz);
				}else if(object instanceof byte[]){
					if(typeReference != null) {
						return objectMapper.readValue((byte[]) object, typeReference);
					}
					return objectMapper.readValue((byte[]) object, clazz);
				}else {
					return (T) object;
				}
			}else{
				throw new JMSException("unknow message type: " + message.getClass().getSimpleName());
			}
		}catch (IOException e){
			logger.error("msg={}", e.getMessage(), e);
			throw new JMSException(e.getMessage());
		}

	}

}
