package com.mageddo.jms.vo;

import java.io.Serializable;

/**
 * Created by elvis on 11/05/17.
 */
public class Color implements Serializable {

	public static final Color WHITE = new Color("WHITE");
	public static final Color BLUE = new Color("BLUE");
	public static final Color RED = new Color("RED");

	private String name;
	private long id;

	public Color() {
	}

	public Color(String name) {
		this.name = name;
	}

	public Color(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Color color = (Color) o;

		return name.equals(color.name);
	}

	@Override
	public String toString() {
		return "id=" + this.id + ", name=" + this.name;
	}
}
