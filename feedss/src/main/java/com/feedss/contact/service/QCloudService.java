package com.feedss.contact.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.feedss.contact.entity.AccountUsersig;
import com.feedss.contact.qcloud.GenerateTLSSignatureUtil;
import com.feedss.contact.qcloud.QCloudMessageUtil;
import com.feedss.contact.repository.AccountUsersigRepository;

@Component
public class QCloudService {

	@Autowired
	private AccountUsersigRepository accountUsersigRepository;
	
	@Autowired
	private QCloudMessageUtil qcloudMessageUtil;
	
	@Autowired
	private GenerateTLSSignatureUtil generateTLSSignatureUtil;
	
	
	public String qcloudImportAccount(String accountId, String nickname, String photo){
		String userSig = this.getUserSig(accountId);
		if(!StringUtils.isEmpty(userSig)){
			boolean isSuccess = qcloudMessageUtil.importAccount(accountId, nickname, photo);
			if(!isSuccess) return null;
			isSuccess = qcloudMessageUtil.addAttrToUser(QCloudMessageUtil.ATTR_ID, accountId, accountId);
			if(isSuccess) return userSig;
			else return "";
		}
		return userSig;
	}
	
	public void setQCloudTag(String accountId, String tag){
		qcloudMessageUtil.addTagToUser(tag, accountId);
	}
	
	public String getUserSig(String accountId){
		
		AccountUsersig accountUsersig = accountUsersigRepository.findByAccountId(accountId);
		
		if(accountUsersig==null){
			String userSig = generateTLSSignatureUtil.getUserSig(accountId);
			accountUsersig = new AccountUsersig();
			accountUsersig.setAccountId(accountId);
			accountUsersig.setUserSig(userSig);
			accountUsersig = accountUsersigRepository.save(accountUsersig);
		}
		return accountUsersig.getUserSig();
	}
	
	public boolean setUserInfo(String accountId, String nickname, String photo){
//		 boolean flag = qcloudMessageUtil.uploadUserInfo(accountId, nickname, photo);
		boolean flag = qcloudMessageUtil.importAccount(accountId, nickname, photo);
		 return flag;
	}
	
}

