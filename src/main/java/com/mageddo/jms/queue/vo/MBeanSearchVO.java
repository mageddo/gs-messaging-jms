package com.mageddo.jms.queue.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by elvis on 15/06/17.
 */
public class MBeanSearchVO {

	@JsonProperty("value")
	private String[] mBeans;
	private long timestamp;
	private int status;

	public String getMainBean(){
		if(mBeans.length > 0){
			return mBeans[0];
		}
		return null;
	}

	public String[] getmBeans() {
		return mBeans;
	}

	public void setmBeans(String[] mBeans) {
		this.mBeans = mBeans;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
