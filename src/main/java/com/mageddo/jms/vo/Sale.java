package com.mageddo.jms.vo;

/**
 * Created by elvis on 23/05/17.
 */
public class Sale {

	private String description;

	public Sale(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Sale{" +
			"description='" + description + '\'' +
			'}';
	}
}
