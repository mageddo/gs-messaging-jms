package com.mageddo.jms.vo;

/**
 * Created by elvis on 28/05/17.
 */
public class Withdraw {

	private double value;
	private String from;
	private String to;

	public Withdraw() {
	}

	public Withdraw(double value, String from, String to) {
		this.value = value;
		this.from = from;
		this.to = to;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
}
