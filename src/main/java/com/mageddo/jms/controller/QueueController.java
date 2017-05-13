package com.mageddo.jms.controller;

import org.apache.activemq.Message;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.jms.JMSException;
import javax.jms.TextMessage;

/**
 * Created by elvis on 13/05/17.
 */
@Controller
public class QueueController {

	@Autowired
	private JmsTemplate jmsTemplate;

	@RequestMapping("/new/message")
	@ResponseBody
	public void postMessage(@RequestParam("dest") String destination, @RequestParam(value = "msg", defaultValue = "x") String message) throws JMSException {
//		Message msg = MessageBuilder.withPayload(message).setHeader("dlq", "mydlqheader")
		ActiveMQTextMessage msg = new ActiveMQTextMessage();
		msg.setText(message);
		msg.setStringProperty("dlq", "dlqProp");
//		msg.h
		jmsTemplate.convertAndSend(destination, message);
	}
}
