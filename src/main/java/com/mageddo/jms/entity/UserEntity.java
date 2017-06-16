package com.mageddo.jms.entity;

import org.springframework.jdbc.core.RowMapper;

import java.util.Date;

/**
 * Created by elvis on 15/06/17.
 */
public class UserEntity {

	private Integer id;
	private String name;
	private Status status;
	private Date enqueuedDate;

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

	public Date getEnqueuedDate() {
		return enqueuedDate;
	}

	public void setEnqueuedDate(Date enqueuedDate) {
		this.enqueuedDate = enqueuedDate;
	}

	public static RowMapper<UserEntity> mapper() {
		return (rs, rowNum) -> {
			final UserEntity userEntity = new UserEntity();
			userEntity.setId(rs.getInt("IDT_USER"));
			userEntity.setName(rs.getString("NAM_USER"));
			userEntity.setStatus(Status.fromCode(rs.getString("IND_STATUS")));
			userEntity.setEnqueuedDate(rs.getTimestamp("DAT_ENQUEUED"));
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

	@Override
	public String toString() {
		return "UserEntity{" +
			"id=" + id +
			", name='" + name + '\'' +
			", status=" + status +
			'}';
	}
}
