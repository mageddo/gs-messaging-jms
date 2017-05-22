package com.mageddo.jms.receiver;

import com.mageddo.jms.queue.DestinationConstants;
import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.vo.Color;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(MailReceiver.class);

	@Autowired
	private JmsTemplate jmsTemplate;

	long id=0;

//	@Scheduled(fixedDelay = 500)
	public void postMail() throws JMSException, IOException {

		final Color colorName = new Color[]{Color.BLUE, Color.RED, Color.WHITE}[new Random().nextInt(3)];
		final Color color = new Color(++id, colorName.getName());

		LOGGER.info("status=color-post, color={}", color);
		final ActiveMQObjectMessage message = new ActiveMQObjectMessage();
		message.setObject(color);
		message.compress();
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
	public void genericReceiveMessage(Color color) throws InterruptedException {

		Thread.sleep(250);

		LOGGER.info("status=GEN-color-receiver, color={}", color);
	}

	@JmsListener(
		destination = DestinationConstants.COLOR,
		containerFactory = DestinationConstants.FACTORY_RED_COLOR + "Factory",
		selector = "color='RED'"
	)
	public void receiveMessage(ObjectMessage message) throws InterruptedException, JMSException {
		LOGGER.info("status=RED-color-receiver, color={}, status=begin", message.getObject());
		if (new Random().nextInt(4) == 3) {
			Thread.sleep(250);
			LOGGER.info("status=RED-color-receiver, color={}, status=sucess", message.getObject());
		} else {
			LOGGER.error("status=RED-color-receiver, color={}, status=error", message.getObject());
			throw new RuntimeException("failed");
		}
	}

}
