package com.mageddo.jms.receiver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mageddo.jms.queue.DestinationEnum;
import com.mageddo.jms.queue.JsonConverter;
import com.mageddo.jms.service.QueueService;
import com.mageddo.jms.vo.YoutubeNotificationVO;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageNotWriteableException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mageddo.jms.queue.DestinationConstants.YOUTUBE_NOTIFICATION;

/**
 * Created by elvis on 15/06/17.
 *
 * Benchmarks results:
 *
 * <table border="1">
 *   <tr>
 *     <th>Mode</th>
 *     <th>Messages QTD</th>
 *     <th align="right">Time (ms)</th>
 *   </tr>
 *   <tr>
 *     <td>PERSISTENT</td>
 *     <td>50k</td>
 *     <td align="right">529590</td>
 *   </tr>
 *   <tr>
 *     <td>NON_PERSISTENT</td>
 *     <td>50k</td>
 *     <td align="right">12854</td>
 *   </tr>
 * </table>
 *
 */
@Component
public class NonPersistentYoutubeNotificationReceiver {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private AtomicInteger subscribers = new AtomicInteger(1);
	private StopWatch stopWatch = new StopWatch();
	private AtomicBoolean started = new AtomicBoolean(false);

	@Autowired
	private QueueService queueService;

	@Autowired
	private JsonConverter jsonConverter;

	@Autowired
	private JmsTemplate jmsTemplate;

//	@Scheduled(fixedDelay = Long.MAX_VALUE)
	@Transactional
	public void doNotify() throws JsonProcessingException, MessageNotWriteableException {
		for(int i=0; i < 50_000; i++){

			jmsTemplate.convertAndSend(
				DestinationEnum.YOUTUBE_NOTIFICATION.getDestination(),
				new YoutubeNotificationVO(subscribers.getAndIncrement()), msg -> {
					msg.setJMSDeliveryMode(DestinationEnum.YOUTUBE_NOTIFICATION.getCompleteDestination().getDeliveryMode());
					return msg;
				}
			);

		}
	}

	@JmsListener(destination = YOUTUBE_NOTIFICATION, containerFactory = "#{queue.get('YOUTUBE_NOTIFICATION').getFactory()}")
	public void consume(final Message msg) throws JMSException {

		if(!started.getAndSet(true)){
			stopWatch.start();
			logger.info("status=started");
		}
		final YoutubeNotificationVO notification =  jsonConverter.readValue(msg, YoutubeNotificationVO.class);
		logger.info("to={}, time={}", notification.getTo(), stopWatch.getTime());

	}
}
