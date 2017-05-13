package com.mageddo.jms.queue;

public interface Queue {

	String getName();
	int getTTL();
	int getRetries();
	int getConsumers();
	int getMaxConsumers();
}