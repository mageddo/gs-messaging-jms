package com.mageddo.jms.queue.config;

import org.springframework.beans.factory.NamedBean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * Created by elvis on 21/05/17.
 */
public class MageddoMessageListenerContainerFactory extends DefaultJmsListenerContainerFactory implements NamedBean {

	private final DefaultMessageListenerContainer container;
	private String beanName;

	public MageddoMessageListenerContainerFactory(DefaultMessageListenerContainer container) {
		this.container = container;
	}
	public MageddoMessageListenerContainerFactory(DefaultMessageListenerContainer container, String beanName) {
		this.container = container;
		this.beanName = beanName;
	}

	@Override
	protected DefaultMessageListenerContainer createContainerInstance() {
		return this.container;
	}

	public DefaultMessageListenerContainer getContainer() {
		return container;
	}

	@Override
	public String getBeanName() {
		return this.beanName;
	}

	public void stop(){
		this.getContainer().stop();
	}
}
