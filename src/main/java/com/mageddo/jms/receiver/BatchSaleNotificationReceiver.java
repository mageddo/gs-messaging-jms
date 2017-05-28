package com.mageddo.jms.receiver;

import com.mageddo.jms.queue.DestinationConstants;
import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.queue.container.BatchMessageListener;
import com.mageddo.jms.queue.container.BatchListMessageListenerContainer;
import com.mageddo.jms.service.SaleService;
import com.mageddo.jms.vo.Sale;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.List;

import static com.mageddo.jms.utils.QueueUtils.configureRedelivery;
import static com.mageddo.jms.utils.QueueUtils.createContainer;

/**
 * Created by elvis on 23/05/17.
 */

@Component
public class BatchSaleNotificationReceiver implements BatchMessageListener {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private SaleService saleService;

	@Autowired
	private PlatformTransactionManager txManager;

	@Override
	public List<ActiveMQTextMessage> onMessage(List<ActiveMQTextMessage> messages) throws JMSException {
		logger.info("status=onMessage, size={}", messages.size());

		final List<ActiveMQTextMessage> notConsumed = new ArrayList<>();
		for (final ActiveMQTextMessage saleMsg: messages) {

//			final boolean success = new Random().nextBoolean();
			final boolean success = false;
			logger.info("status=onMessage, status={}, msg={}", success ? "success" : "redelivery", saleMsg.getText());
			if (success){
				saleService.completeSale(new Sale(saleMsg.getText()));
			} else {
				notConsumed.add(saleMsg);
			}

		}
		return notConsumed;
	}

	@Bean(name = DestinationConstants.SALE + "Container", initMethod = "start", destroyMethod = "stop")
	public DefaultMessageListenerContainer container(ActiveMQConnectionFactory cf, BatchSaleNotificationReceiver receiver){

		final DestinationEnum queue = DestinationEnum.SALE;
		final RedeliveryPolicy redeliveryPolicy = configureRedelivery(cf, queue);
		final DefaultMessageListenerContainer container = createContainer(
			cf, queue.getCompleteDestination(), new BatchListMessageListenerContainer(1, redeliveryPolicy)
		);
		container.setDestination(queue.getDestination());
		container.setMessageListener(receiver);

		return container;

	}

}
