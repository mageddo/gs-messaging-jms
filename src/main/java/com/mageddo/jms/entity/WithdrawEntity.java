package com.mageddo.jms.entity;

import java.util.Date;

/**
 * Created by elvis on 28/05/17.
 */
public class WithdrawEntity {
	private int id;
	private char type;
	private char status;
	private double value;
	private Date creationDate;
	private Date updateDate;

	public WithdrawEntity(int id, char status, char type, double value) {
		this.id = id;
		this.status = status;
		this.type = type;
		this.value = value;
		setCreationDate(new Date());
		setUpdateDate(getCreationDate());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
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

	public char getStatus() {
		return status;
	}

	public void setStatus(char status) {
		this.status = status;
	}

	public WithdrawStatus getStatusEnum(){
		return WithdrawStatus.fromStatus(this.getStatus());
	}

	public void setStatusEnum(WithdrawStatus status){
		setStatus(status.getStatus());
	}

	public enum WithdrawType {
		BANK('B'),
		RFID('R');

		private char type;

		WithdrawType(char type) {
			this.type = type;
		}

		public char getType() {
			return type;
		}
	}

	public enum WithdrawStatus {

		OPEN('O'),
		PROCESING('P'),
		ERROR('E'),
		COMPLETED('C');

		private char status;

		WithdrawStatus(char status) {
			this.status = status;
		}

		public char getStatus() {
			return status;
		}

		public static WithdrawStatus fromStatus(char status){
			for (WithdrawStatus withdrawStatus : values()) {
				if(withdrawStatus.getStatus() == status){
					return withdrawStatus;
				}
			}
			return null;
		}
	}
}
