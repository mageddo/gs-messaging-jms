package com.mageddo.jms.receiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mageddo.jms.queue.DestinationConstants;
import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.vo.Color;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import java.io.IOException;
import java.util.Random;

/**
 * Created by elvis on 11/05/17.
 */
@Component
public class ColorReceiver {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	long id=0;

//	@Scheduled(fixedDelay = 500)
	public void postMail() throws JMSException, IOException {

		final Color colorName = new Color[]{Color.BLUE, Color.RED, Color.WHITE}[new Random().nextInt(3)];
		final Color color = new Color(++id, colorName.getName());

		logger.info("status=color-post, color={}", color);
		final ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setText(objectMapper.writeValueAsString(color));
		message.setProperty("color", color.getName());

		jmsTemplate.convertAndSend(DestinationEnum.COLOR_TOPIC.getDestination(), message);
	}


	/**
	 * the destination ClientId have not necessary exists (it means that his name can be a fancy name), the unique requirement is that
	 * the containers clientId need to be different between each other
	 * @param color
	 * @throws InterruptedException
	 */
	@JmsListener(
		destination = DestinationConstants.COLOR,
		containerFactory = DestinationConstants.COLOR + "Factory",
		selector = "color <> 'RED'"
	)
	public void genericReceiveMessage(ActiveMQTextMessage color) throws InterruptedException, JMSException {

		Thread.sleep(250);

		logger.info("status=GEN-color-receiver, color={}", color.getText());
	}

	@JmsListener(
		destination = DestinationConstants.COLOR,
		containerFactory = DestinationConstants.FACTORY_RED_COLOR + "Factory",
		selector = "color='RED'"
	)
	public void receiveMessage(ActiveMQTextMessage message) throws InterruptedException, JMSException {
		logger.info("status=RED-color-receiver, color={}, status=begin", message.getText());
		if (new Random().nextInt(4) == 3) {
			Thread.sleep(250);
			logger.info("status=RED-color-receiver, color={}, status=sucess", message.getText());
		} else {
			logger.error("status=RED-color-receiver, color={}, status=error", message.getText());
			throw new RuntimeException("failed");
		}
	}

}
