package com.mageddo.jms.entity;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;

import java.util.Date;

/**
 * Created by elvis on 21/05/17.
 */
public class DestinationParameterEntity {

	private int id;
	private String name;
	private int consumers;
	private int maxConsumers;
	private int ttl;
	private int retries;

	private Date creationDate;
	private Date updateDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getConsumers() {
		return consumers;
	}

	public void setConsumers(int consumers) {
		this.consumers = consumers;
	}

	public int getMaxConsumers() {
		return maxConsumers;
	}

	public void setMaxConsumers(int maxConsumers) {
		this.maxConsumers = maxConsumers;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
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

	public static RowMapper<DestinationParameterEntity> rowMapper() {
		return (rs, index) -> {
			final DestinationParameterEntity entity = new DestinationParameterEntity();
			entity.setId(rs.getInt("IDT_DESTINATION_PARAMETER"));
			entity.setName(rs.getString("NAM_DESTINATION_PARAMETER"));
			entity.setConsumers(rs.getShort("NUM_CONSUMERS"));
			entity.setMaxConsumers(rs.getShort("NUM_MAX_CONSUMERS"));
			entity.setTtl(rs.getInt("NUM_TTL"));
			entity.setRetries(rs.getShort("NUM_RETRIES"));
			entity.setCreationDate(rs.getTimestamp("DAT_CREATION"));
			entity.setUpdateDate(rs.getTimestamp("DAT_UPDATE"));
			return entity;
		};
	}
}
