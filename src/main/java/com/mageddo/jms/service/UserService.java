package com.mageddo.jms.service;

import com.mageddo.jms.dao.UserDAO;
import com.mageddo.jms.entity.UserEntity;
import com.mageddo.jms.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elvis on 15/06/17.
 */
@Service
public class UserService {

	@Autowired
	private UserDAO userDAO;

	public void register(UserVO userVO){
		userDAO.create(new UserEntity(userVO.getName(), UserEntity.Status.PENDING));
	}

	public List<UserEntity> findNotEnqueuedRegistrations() {
		// select from user limit 0,1000
		//
		return new ArrayList<>();
	}

	public void markAsEnqueued(List<UserEntity> entities){
		// UPDATE SET STATUS=Q
	}

	public void markAsCompleted(UserEntity userEntity) {
		// UPDATE SET STATUS=C
	}
}
