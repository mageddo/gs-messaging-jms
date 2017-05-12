package com.mageddo.jms.receiver;

import com.mageddo.jms.Email;
import com.mageddo.jms.vo.Color;
import org.apache.activemq.Message;
import org.apache.activemq.broker.region.virtual.VirtualTopic;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQTopic;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(MailReceiver.class);

	@Autowired
	private JmsTemplate jmsTemplate;

	long id=0;

	@Scheduled(fixedDelay = 500)
	public void postMail() throws JMSException, IOException {

		final Color colorName = new Color[]{Color.BLUE, Color.RED, Color.WHITE}[new Random().nextInt(3)];
		final Color color = new Color(++id, colorName.getName());

		LOGGER.info("status=color-post, color={}", color);
		final ActiveMQObjectMessage message = new ActiveMQObjectMessage();
		message.setObject(color);
		message.compress();
		message.setProperty("color", color.getName());

//		final VirtualTopic virtualTopic = new VirtualTopic();
//		virtualTopic.setPrefix("VirtualTopic");
//		virtualTopic.setName("color");
		jmsTemplate.convertAndSend(new ActiveMQTopic("VirtualTopic.color"), message);
	}


//	@JmsListener(destination = "color", containerFactory = "mailContainer", subscription = "colorx")

	/**
	 * the distination ClientId have not necessary exists (it means that his name can be a fancy name), the unique requirement is that
	 * the containers clientId need to be different between each other
	 * @param color
	 * @throws InterruptedException
	 */
	@JmsListener(
		destination = "Consumer.colorClient.VirtualTopic.color",
		containerFactory = "colorContainer"
//		selector = "color <> 'RED'"
	)
	public void genericReceiveMessage(Color color) throws InterruptedException {

		Thread.sleep(250);

		LOGGER.info("status=GEN-color-receiver, color={}", color);
	}

//	@JmsListener(destination = "color", containerFactory = "redColorContainer", selector = "color='RED'", subscription = "client-1")
	@JmsListener(
//		destination = "Consumer.redColorContainer.VirtualTopic.color",
		destination = "Consumer.colorClient.VirtualTopic.color",
		containerFactory = "redColorContainer", selector = "color='RED'"
	)
	public void receiveMessage(ObjectMessage message) throws InterruptedException, JMSException {

		Thread.sleep(250);

		LOGGER.info("status=RED-color-receiver, color={}", message.getObject());
	}

}
