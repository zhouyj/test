package com.feedss.user.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.feedss.base.JsonData;

/**
 * Created by qinqiang on 2016/7/30.
 */

@Service
public interface SmsService {
	
	public JsonData sendRandom(String mobile, String type);
	
	
	/**
	 * 发送短信接口
	 */
	public boolean sendSms(String mobile,String content);
}
