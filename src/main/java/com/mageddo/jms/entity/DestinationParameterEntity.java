package com.mageddo.jms.entity;

import com.mageddo.jms.queue.DestinationEnum;

import java.util.Date;

/**
 * Created by elvis on 21/05/17.
 */
public class DestinationParameterEntity {

	private int id;
	private String name;
	private int consumers;
	private int maxConsumers;
	private int ttl;
	private int retries;

	private Date creationDate;
	private Date updateDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getConsumers() {
		return consumers;
	}

	public void setConsumers(int consumers) {
		this.consumers = consumers;
	}

	public int getMaxConsumers() {
		return maxConsumers;
	}

	public void setMaxConsumers(int maxConsumers) {
		this.maxConsumers = maxConsumers;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
}
