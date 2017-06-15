package com.mageddo.jms.vo;

/**
 * Created by elvis on 15/06/17.
 */
public class ByteMessageVO {
	private int id;
	private byte[] bytes;

	public ByteMessageVO() {
	}

	public ByteMessageVO(int id, byte[] bytes) {
		this.id = id;
		this.bytes = bytes;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	@Override
	public String toString() {
		return "ByteMessageVO{" +
			"id=" + id +
			", length=" + bytes.length +
			'}';
	}
}
