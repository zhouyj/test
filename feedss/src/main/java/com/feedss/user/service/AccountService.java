package com.feedss.user.service;

import java.util.Date;
import java.util.List;

import com.feedss.base.JsonData;
import com.feedss.user.entity.Account;
import com.feedss.user.entity.AccountTransaction;
import com.feedss.user.entity.AccountTransaction.AccoutTransactionSourceType;
import com.feedss.user.entity.TimeCard;
import com.feedss.user.model.UserVo;

public interface AccountService {

	public Account getByUserId(String userId);

	public Account createAccount(String userId);

	/**
	 * 带有balance计算逻辑
	 * 
	 * @param userId
	 * @return
	 */
	public Account getAccountInfo(String userId);

	/**
	 * 奖励
	 * 
	 * @param userId
	 */
	public void reward(String userId, int count, String sourceId, AccoutTransactionSourceType sourceType);

	public TimeCard getTimeCard(String serialNumber, String password);

	public int activeTimeCard(String serialNumber, String userId);

	public TimeCard getTimeCardByUser(String userId);

	public Account save(Account account);

	public boolean transfer(UserVo fromUser, UserVo toUser, AccoutTransactionSourceType sourceType, String sourceId,
			String type, String description, int change);

	public boolean isExist(String userId, AccoutTransactionSourceType sourceType, Date start, Date end);
	
	/**
	 * 查询交易记录
	 * @param userId
	 * @param cursorId
	 * @param pageSize
	 * @return
	 */
	public List<AccountTransaction> selectAccountTransactions(String userId, String cursorId,Integer pageSize, Integer direction);
	
	public JsonData getAccountBalance(String userId);
}
