package com.mageddo.jms.dao;

import com.mageddo.jms.entity.UserEntity;

import java.util.List;

/**
 * Created by elvis on 15/06/17.
 */
public interface UserDAO {
	void create(UserEntity userEntity);

	List<UserEntity> findNotEnqueuedRegistrations(int maxResults);

	void changeStatus(List<UserEntity> entities, UserEntity.Status queued);

	void enqueue(List<UserEntity> entities);
}
