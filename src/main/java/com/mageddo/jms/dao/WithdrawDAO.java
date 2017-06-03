package com.mageddo.jms.dao;

import com.mageddo.jms.entity.WithdrawEntity;

import java.util.List;

/**
 * Created by elvis on 29/05/17.
 */
public interface WithdrawDAO {
	void createWithdraw(List<WithdrawEntity> withdrawEntity);

	List<WithdrawEntity> findWithdrawsByStatus(WithdrawEntity.WithdrawStatus withdrawStatus);

	int changeStatus(int id, WithdrawEntity.WithdrawStatus statusEnum);
}
