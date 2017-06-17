package com.mageddo.jms.queue;

public interface MGDestination {

	String getId();
	String getName();
	int getTTL();
	int getRetries();
	int getConsumers();
	int getMaxConsumers();
}