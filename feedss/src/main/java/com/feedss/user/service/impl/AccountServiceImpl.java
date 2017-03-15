package com.feedss.user.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.feedss.base.Constants;
import com.feedss.base.JsonData;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.user.entity.Account;
import com.feedss.user.entity.AccountTransaction;
import com.feedss.user.entity.AccountTransaction.AccoutTransactionSourceType;
import com.feedss.user.entity.TimeCard;
import com.feedss.user.entity.TimeCard.TimeCardStatus;
import com.feedss.user.model.UserVo;
import com.feedss.user.repository.AccountRepository;
import com.feedss.user.repository.AccountTransactionRepository;
import com.feedss.user.repository.TimeCardRepository;
import com.feedss.user.service.AccountService;

/**
 * Created by qin.qiang on 2016/8/2 0002.
 */
@Service
public class AccountServiceImpl implements AccountService {
	private static final Logger Logger = LoggerFactory.getLogger(AccountServiceImpl.class);
	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private AccountTransactionRepository accountTransactionRepository;

	@Autowired
	private TimeCardRepository timeCardRepository;
	
	@Autowired
	private ConfigureUtil configureUtil;
	
	@Autowired
	private EntityManager entityManager;

	@Override
	public Account getByUserId(String userId) {
		Account account = accountRepository.findByUserId(userId);
		return account;
	}

	@Override
	public Account createAccount(String userId) {
		Account account = new Account();
		account.setRank(1);
		account.setType("");
		account.setStatus(1);
		account.setBalance(0);
		account.setUserId(userId);
		account.setUpdated(DateTime.now().toDate());
		account = accountRepository.save(account);
		return account;
	}

	@Override
	public Account getAccountInfo(String userId) {
		Account account = getByUserId(userId);
		if (account == null)
			account = new Account();
		if (configureUtil.getConfigIntValue(Constants.ACCOUNT_TRANSACTION_HASEXPIRED) > 0) {
			Integer balance = accountTransactionRepository.getBalance(userId);
			if (balance == null) {
				balance = 0;
			}
			account.setBalance(balance);
		}
		return account;
	}

	@Override
	public void reward(String userId, int increase, String sourceId, AccoutTransactionSourceType sourceType) {
		// TODO 奖励的实现
		Account account = getAccountInfo(userId); // 余额
		int newBalance = account.getBalance() + increase; // 最新值
		Date expired = AccountTransaction.UnExpiredTime;
		int systemPresentExpired = configureUtil.getConfigIntValue(Constants.ACCOUNT_TRANSACTION_SYSTEMPRESEND_EXPIRED);
		if (configureUtil.getConfigIntValue(Constants.ACCOUNT_TRANSACTION_HASEXPIRED)>0 &&  systemPresentExpired> 0) {
			expired = DateTime.now().plusDays(systemPresentExpired).toDate();

		}
		String name = "系统赠送";
		// 生成余额变化记录
		AccountTransaction transaction = new AccountTransaction(userId, account.getUuid(),
				sourceType.name(), sourceId, increase, account.getBalance(), newBalance,
				expired, name);

		Logger.info("生成余额变更记录transaction: " + JSON.toJSONString(transaction));
		accountTransactionRepository.save(transaction);

		// 更新余额
		account.setBalance(newBalance);
		Logger.info("更新余额account: " + JSON.toJSONString(account));
		accountRepository.save(account);
	}

	@Override
	public TimeCard getTimeCard(String serialNumber, String password) {
		TimeCard card = timeCardRepository.find(serialNumber);
		if (card == null)
			return null;
		if (StringUtils.isEmpty(password)) {
			if (StringUtils.isEmpty(card.getPassword())) {
				return card;
			} else {
				return null;
			}
		} else if (!password.equals(card.getPassword())) {
			return null;
		}
		return card;
	}

	@Override
	public int activeTimeCard(String serialNumber, String userId) {
		TimeCard card = timeCardRepository.find(serialNumber);
		if (card != null) {
			card.setActiveTime(new Date().getTime());
			card.setStatus(TimeCardStatus.ACTIVIED.ordinal());
			card.setUserId(userId);
			// TODO 赠送积分
			timeCardRepository.save(card);
			return 1;
		}
		return 0;
	}

	@Override
	public TimeCard getTimeCardByUser(String userId) {
		return timeCardRepository.findByUser(userId);
	}

	@Override
	public Account save(Account account) {
		return accountRepository.save(account);
	}

	public boolean transfer(UserVo fromUser, UserVo toUser, AccoutTransactionSourceType sourceType,
			String sourceId, String type, String description, int change) {
		Account fromAccount = getAccountInfo(fromUser.getUuid());
		int originBalance = fromAccount.getBalance();
		int balance = originBalance - change;
		if (balance < 0) {
			return false;
		}
		String fromName="转账", toName = "转账";
		if(sourceType==AccoutTransactionSourceType.interactionNew){
			fromName = configureUtil.getConfig("interactionNewFromDescription");
			toName = configureUtil.getConfig("interactionNewToDescription");
		}else if(sourceType==AccoutTransactionSourceType.interactionReject){
			fromName = configureUtil.getConfig("interactionRejectFromDescription");
			toName = configureUtil.getConfig("interactionRejectToDescription");
		}else if(sourceType==AccoutTransactionSourceType.interactionOverdua){
			fromName = configureUtil.getConfig("interactionOverduaFromDescription");
			toName = configureUtil.getConfig("interactionOverduaToDescription");
		}
		fromName = fromName.replace("$type", type).replace("$description", description);
		toName = toName.replace("$type", type).replace("$description", description);
				
		
		AccountTransaction fromTransaction = new AccountTransaction(fromUser.getUuid(), fromAccount.getUuid(),
				sourceType.name(), sourceId, -change, originBalance, balance, AccountTransaction.UnExpiredTime, fromName);
		// 插入余额变更记录
		accountTransactionRepository.save(fromTransaction);
		fromAccount.setBalance(balance);
		save(fromAccount);

		Account toAccount = getAccountInfo(toUser.getUuid());
		originBalance = toAccount.getBalance();
		balance = originBalance + change;
		AccountTransaction toTransaction = new AccountTransaction(toUser.getUuid(), toAccount.getUuid(), sourceType.name(),
				sourceId, change, originBalance, balance, AccountTransaction.UnExpiredTime, toName);
		// 插入余额变更记录
		accountTransactionRepository.save(toTransaction);
		toAccount.setBalance(balance);
		save(toAccount);
		return true;
	}

	@Override
	public boolean isExist(String userId, AccoutTransactionSourceType sourceType, Date start, Date end) {
		int count = accountTransactionRepository.getCount(userId, sourceType.name(), start, end);
		return count>0?true:false;
	}

	@Override
	public List<AccountTransaction> selectAccountTransactions(String userId, String cursorId, Integer pageSize, Integer direction) {
		Map<String,Object> paraMap = new HashMap<String,Object>();
		StringBuilder sqlSB = new StringBuilder("select at from AccountTransaction at where at.userId=:userId ");
		paraMap.put("userId", userId);
			if(StringUtils.isNotBlank(cursorId)){
				AccountTransaction transaction = accountTransactionRepository.findOne(cursorId);
				if(transaction!=null){
					if(direction.intValue() ==1){//上一页
						sqlSB.append(" and (at.created >:created or (at.created = :created and at.id > :id))");
					}else{//下一页
						sqlSB.append(" and (at.created <:created or (at.created = :created and at.id < :id))");
					}
					paraMap.put("created", transaction.getCreated());
					paraMap.put("id", transaction.getId());
				}
			}
			sqlSB.append(" order by at.created desc,at.id desc");

		TypedQuery<AccountTransaction> result = entityManager.createQuery(sqlSB.toString(), AccountTransaction.class).setFirstResult(0).setMaxResults(pageSize);;
		paraMap.forEach((k,v)->{
			result.setParameter(k, v);
		});
		List<AccountTransaction> list = result.getResultList();
		return list == null ? new ArrayList<AccountTransaction>():list;
	}

	@Override
	public JsonData getAccountBalance(String userId) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		Account account = getAccountInfo(userId);
		if (account == null) {
			account = createAccount(userId);
		}

		int currencyRatio = 1;
		int moneyRatio = 1;
		String ratio = configureUtil.getConfig(Constants.RECHARGE_RATIO);
		if (StringUtils.isNotBlank(ratio) && ratio.indexOf(":") > 0) {
			moneyRatio = Integer.parseInt(ratio.split(":")[0]);
			currencyRatio = Integer.parseInt(ratio.split(":")[1]);
		}

		String[] strs = configureUtil.getConfig(Constants.ACCOUNT_BUYLIST).split(",");
		List<HashMap<String, Object>> buylist = new ArrayList<HashMap<String, Object>>();
		for (String str : strs) {
			HashMap<String, Object> hashMap = new HashMap<String, Object>();
			int lvPrice = Integer.parseInt(str);
			double rmbPrice = (double) lvPrice * moneyRatio / currencyRatio;
			hashMap.put("lvbCount", lvPrice);
			hashMap.put("rmbPrice", rmbPrice);
			buylist.add(hashMap);
		}

		data.put("balance", account.getBalance());
		data.put("buylist", buylist);
		return JsonData.successNonNull(data);
	}
}
