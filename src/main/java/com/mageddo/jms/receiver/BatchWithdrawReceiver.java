package com.mageddo.jms.receiver;

import com.mageddo.jms.queue.DestinationConstants;
import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.queue.container.BatchListMessageListenerContainer;
import com.mageddo.jms.queue.container.BatchMessage;
import com.mageddo.jms.queue.container.BatchMessageListenerContainer;
import com.mageddo.jms.service.WithdrawService;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.JMSException;

import static com.mageddo.jms.utils.QueueUtils.configureRedelivery;
import static com.mageddo.jms.utils.QueueUtils.createContainer;

/**
 * Created by elvis on 28/05/17.
 */
@Component
public class BatchWithdrawReceiver {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private WithdrawService withdrawService;

	@Autowired
	private PlatformTransactionManager txManager;

	public void onMessage(final BatchMessage withdraws) throws JMSException {
		logger.info("status=onMessage, size={}", withdraws.size());

		for (final ActiveMQMessage withdrawMsg: withdraws.messages()) {

//			final boolean success = new Random().nextBoolean();
			final boolean success = false;
			if (success){
				withdrawService.doWithdraw(((ActiveMQTextMessage)withdrawMsg).getText());
			} else {
				withdraws.onError(withdrawMsg);
			}

		}
	}

	@Bean(name = DestinationConstants.WITHDRAW + "Container", initMethod = "start", destroyMethod = "stop")
	public DefaultMessageListenerContainer container(ActiveMQConnectionFactory cf, BatchWithdrawReceiver receiver){

		final DestinationEnum queue = DestinationEnum.WITHDRAW;
		configureRedelivery(cf, queue);
		final DefaultMessageListenerContainer container = createContainer(
			cf, queue.getCompleteDestination(), new BatchMessageListenerContainer(5)
		);
		container.setDestination(queue.getDestination());
		container.setMessageListener(receiver);

		return container;

	}
}
