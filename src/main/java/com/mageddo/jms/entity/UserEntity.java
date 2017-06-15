package com.mageddo.jms.entity;

/**
 * Created by elvis on 15/06/17.
 */
public class UserEntity {

	private Integer id;
	private String name;
	private Status status;

	public UserEntity() {
	}

	public UserEntity(String name, Status status) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public enum Status {
		PENDING("P"),
		QUEUED("Q"),
		COMPLETED("C")

		;

		private final String code;

		Status(String statusCode) {
			this.code = statusCode;
		}

		public String getCode() {
			return code;
		}
	}
}
