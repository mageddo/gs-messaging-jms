package com.mageddo.jms.queue.container;

import javax.jms.Message;
import java.util.List;

/**
 * Created by elvis on 23/05/17.
 */
public interface BatchMessageListener {
	void onMessage(List<Message> messages);
}
