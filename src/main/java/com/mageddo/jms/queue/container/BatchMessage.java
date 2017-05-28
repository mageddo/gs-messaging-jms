package com.mageddo.jms.queue.container;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.JmsUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * As BatchMessage implements the javax.jms.Message interface it fits perfectly into the DMLC - only caveat is that SimpleMessageConverter dont know how to convert it to a Spring Integration Message - but that can be helped.
 * As BatchMessage will only serve as a container to carry the actual javax.jms.Message's from DMLC to the MessageListener it need not provide meaningful implementations of the methods of the interface as long as they are there.
 */
class BatchMessage extends ActiveMQMessage {

	private static final String DELIVERIES = "deliveries";

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final BatchMessageListenerContainer container;

	private List<ActiveMQMessage> messages = new ArrayList<>();
	private Session session;
	private MessageProducer dlqProducer;
	private MessageProducer queueProducer;

	BatchMessage(BatchMessageListenerContainer container) {
		this.container = container;
	}

	private long getDeliveries(final Message message) throws JMSException {
		final String deliveries = message.getStringProperty(DELIVERIES);
		return StringUtils.isBlank(deliveries) ? 0 : Long.parseLong(deliveries);
	}

	private ActiveMQConnectionFactory getActiveMQConnection() {
		return ((ActiveMQConnectionFactory) container.getConnectionFactory());
	}

	/**
	 * Add message to the collection of messages and return true if the batch meets the criteria for releasing it to the MessageListener.
	 */
	boolean releaseAfterMessage(ActiveMQMessage message) {
		if (message != null) {
			this.messages.add(message);
		}
		// Are we ready to release?
		return message == null || this.messages.size() >= this.container.batchSize;
	}

	void setSession(Session session) {
		this.session = session;
	}

	List<ActiveMQMessage> getMessages() {
		return messages;
	}

	public List<ActiveMQMessage> messages() {
		return Collections.unmodifiableList(messages);
	}

	public void onError(final ActiveMQMessage message) throws JMSException, IOException {
		if(this.session == null){
			throw new IllegalStateException("Session can not be null");
		}
		if(!this.messages.contains(message)){
			throw new IllegalStateException(String.format("%s is not a message of this batch", message.getJMSMessageID()));
		}

		final RedeliveryPolicy redeliveryPolicy = getActiveMQConnection()
			.getRedeliveryPolicyMap()
			.getEntryFor((ActiveMQDestination) container.getDestination());

		if(dlqProducer == null || queueProducer == null){
			dlqProducer = session.createProducer(redeliveryPolicy.getDestination());
			queueProducer = session.createProducer(container.getDestination());
		}

		final long deliveries = getDeliveries(message);
		message.setReadOnlyProperties(false);
		if(deliveries < redeliveryPolicy.getMaximumRedeliveries()){

			// removing schedule to can be scheduled again
			message.removeProperty("scheduledJobId");

			// redelivery policy
			message.setLongProperty(DELIVERIES, deliveries + 1);
			message.setLongProperty(
				ScheduledMessage.AMQ_SCHEDULED_DELAY,
				redeliveryPolicy.getNextRedeliveryDelay(redeliveryPolicy.getRedeliveryDelay())
			);

			message.setReadOnlyProperties(true);
			queueProducer.send(message);

		}else{
			message.removeProperty(DELIVERIES);
			message.setReadOnlyProperties(true);
			dlqProducer.send(message);
			logger.info(
				"status=send-to-dlq, dlq={}, msgId={}", redeliveryPolicy.getDestination().getPhysicalName(),
				message.getJMSMessageID()
			);
		}

	}

	void release() {
		JmsUtils.closeMessageProducer(dlqProducer);
		JmsUtils.closeMessageProducer(queueProducer);
	}
}
