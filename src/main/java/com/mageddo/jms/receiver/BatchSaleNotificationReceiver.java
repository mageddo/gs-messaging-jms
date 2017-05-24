package com.mageddo.jms.receiver;

import com.mageddo.jms.queue.DestinationConstants;
import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.queue.container.BatchMessageListener;
import com.mageddo.jms.queue.container.BatchMessageListenerContainer;
import com.mageddo.jms.service.SaleService;
import com.mageddo.jms.vo.Sale;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import java.util.List;

import static com.mageddo.jms.utils.QueueUtils.configureRedelivery;
import static com.mageddo.jms.utils.QueueUtils.createContainer;

/**
 * Created by elvis on 23/05/17.
 */

@Component
public class BatchSaleNotificationReceiver implements BatchMessageListener{

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private SaleService saleService;

	@Override
	public void onMessage(List<ActiveMQTextMessage> messages) throws JMSException {
		logger.info("status=onMessage, size={}", messages.size());
		for (final ActiveMQTextMessage saleMsg: messages) {
			saleService.completeSale(new Sale(saleMsg.getText()));
		}
	}

	@Bean(name = DestinationConstants.SALE + "Container", initMethod = "start", destroyMethod = "stop")
	public DefaultMessageListenerContainer container(ActiveMQConnectionFactory cf, BatchSaleNotificationReceiver receiver){

		final DestinationEnum queue = DestinationEnum.SALE;
		final DefaultMessageListenerContainer container = createContainer(
			cf, queue.getCompleteDestination(), new BatchMessageListenerContainer(10)
		);
		container.setDestination(queue.getDestination());
		configureRedelivery(cf, queue);
		container.setMessageListener(receiver);

		return container;

	}

}
