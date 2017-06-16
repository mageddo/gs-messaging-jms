package com.mageddo.jms.receiver;

import com.mageddo.jms.entity.UserEntity;
import com.mageddo.jms.queue.DestinationConstants;
import com.mageddo.jms.queue.JsonConverter;
import com.mageddo.jms.service.UserService;
import com.mageddo.jms.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by elvis on 14/06/17. <br/><br/>
 *
 * This is a sample of do not use the message broker as a storage system, it happen in this case because the message producer
 * is faster than the consumers (slow consumers), then our queue manager store will be giant. For this reason we consider:
 *
 * <ul>
 *   <li>Firstly, receive the registration request saving it in database and making noting more (Then it will very fast)</li>
 *   <li>
 *     Write a job to check how many messages are in the broker to consume, then post more if there are just few. See that
 *     here we can re-send lost messages (not ha broker) and prevent high number of messages stored at our broker for no reason
 *     once time it can not consume they because it is slow, then we can make some metrics at this table because here we have the
 *     real state of the processing (like a task job)
*    </li>
 *   <li>Consumes the message with: many consumers(distributed process), high availability(if wanted), redelivery policy, inter process communication, whatever</li>
 * </ul>
 *
 * <b>Conclusion:</b> See that this approach only makes sense when your case is similar with one of the bellow:
 * <ul>
 *   <li>Your broker has not high availability, and change it is not a option or is harder than use this approach</li>
 *   <li>Your broker infrastructure datacenter has not potencial to grant storage of high volume of data</li>
 *   <li>You don't want to spend money with the broker storage one time your database can do it(and was made for it)</li>
 *   <li>You think that store a volume of data much greater than what your consumer can process does not makes any sense, once it can be at the database or whatever</li>
 * </ul>
 *
 * For more reference please see the Posts bellow
 *
 * <ul>
 *   <li><a href="http://ferd.ca/queues-don-t-fix-overload.html">Queues Don't Fix Overload</a></li>
 *   <li><a href="https://softwareengineering.stackexchange.com/questions/300606/design-pattern-for-large-amounts-of-overflowing-data">Design pattern for large amounts of overflowing data?</a></li>
 *   <li><a href="https://softwareengineering.stackexchange.com/questions/326067/slow-throughput-still-worth-using-a-dedicated-message-queue/326079?noredirect=1#comment755912_326079">Slow throughput - still worth using a dedicated message queue</a></li>
 * </ul>
 *
 */
@Component
public class LoadBalancedRegistrationReceiver {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private AtomicInteger counter = new AtomicInteger(1);

	@Autowired
	private UserService userService;

	@Autowired
	private JsonConverter jsonConverter;

//	@Scheduled(fixedRate = 1000 / 30)
	public void registrationRequest(){
		final UserVO userVO = new UserVO("User " + counter.getAndIncrement());
		userService.register(userVO);
	}

	@Scheduled(fixedDelay = 60 * 1000)
	public void enqueuer(){
		userService.enqueuePendingRegistrations();
	}

	@JmsListener(destination = DestinationConstants.REGISTRATION, containerFactory = DestinationConstants.REGISTRATION + "Factory")
	public void consume(Message message) throws InterruptedException, JMSException {
		final UserEntity userEntity = jsonConverter.readValue(message, UserEntity.class);
		userService.completeRegistration(userEntity);
	}

}
