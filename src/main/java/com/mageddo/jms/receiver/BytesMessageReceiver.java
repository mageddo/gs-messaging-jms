package com.mageddo.jms.receiver;

import com.mageddo.jms.queue.JsonConverter;
import com.mageddo.jms.vo.ByteMessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by elvis on 15/06/17.
 */
@Component
public class BytesMessageReceiver {

	private static final String BYTES_MESSAGE = "bytesMessage";

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private JsonConverter jsonConverter;

	private AtomicInteger counter = new AtomicInteger(1);

	@Scheduled(fixedDelay = 2000)
	public void post(){
		jmsTemplate.convertAndSend(
			BYTES_MESSAGE,
			new ByteMessageVO(counter.getAndIncrement(), new byte[new Random().nextInt(0)])
		);
	}

	@JmsListener(destination = BYTES_MESSAGE)
	public void consume(final Message message) throws JMSException {
		final ByteMessageVO vo = jsonConverter.readValue(message, ByteMessageVO.class);
		System.out.println(vo);
	}

}
