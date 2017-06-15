package com.mageddo.jms.entity;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

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
		this.status = status;
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

	public static RowMapper<UserEntity> mapper() {
		return (rs, rowNum) -> {
			final UserEntity userEntity = new UserEntity();
			userEntity.setId(rs.getInt("ID"));
			userEntity.setName(rs.getString("NAME"));
			userEntity.setStatus(Status.fromCode(rs.getString("STATUS")));
			return userEntity;
		};
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

		public static Status fromCode(String code) {
			for (Status status : values()) {
				if(status.getCode().equals(code)){
					return status;
				}
			}
			return null;
		}
	}
}
