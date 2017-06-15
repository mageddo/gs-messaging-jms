package com.mageddo.jms.queue.vo;

/**
 * Created by elvis on 15/06/17.
 */
public class QueueDetailsVO {

	private String name;
	private int size;

	public QueueDetailsVO(String name, int size) {
		this.name = name;
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}
}
