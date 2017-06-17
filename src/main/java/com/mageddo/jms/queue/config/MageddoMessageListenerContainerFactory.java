package com.mageddo.jms.queue.config;

import org.springframework.beans.factory.NamedBean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * Created by elvis on 21/05/17.
 */
public class MageddoMessageListenerContainerFactory extends DefaultJmsListenerContainerFactory {

	private final DefaultMessageListenerContainer container;

	public MageddoMessageListenerContainerFactory(DefaultMessageListenerContainer container) {
		this.container = container;
	}

	@Override
	protected DefaultMessageListenerContainer createContainerInstance() {
		return this.container;
	}

	public DefaultMessageListenerContainer getContainer() {
		return container;
	}

	public void stop(){
		this.getContainer().stop();
	}
}
