package com.feedss.user.service.impl;

import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.Constants;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonData;
import com.feedss.base.util.RedisUtil;
import com.feedss.base.util.RestTemplateUtil;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.user.controller.SmsController.SmsType;
import com.feedss.user.entity.User;
import com.feedss.user.service.SmsService;
import com.feedss.user.service.UserService;


/**
 * Created by qinqiang on 2016/7/30.
 */
@Component
public class SmsServiceImpl implements SmsService {
	private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private ConfigureUtil configureUtil;

	@Autowired
	UserService userService;

	@Override
	public boolean sendSms(String mobile, String content) {
		logger.info("mobiles :" + mobile);
		logger.info("content :" + content);
		String url = configureUtil.getConfig(Constants.SMS_HOST) + "?mobile=" + mobile + "&content=" + content;
		logger.info("url " + url);
		// HttpResponse response = HttpRequest.post(url).header("appid",
		// smsAppid).header("apptoken", smsApptoken)
		// .header("Cache-Control", "no-cache").send();
		// String result = response.bodyText();
		HttpHeaders headers = new HttpHeaders();
		headers.add("appid", configureUtil.getConfig(Constants.SMS_APPID));
		headers.add("apptoken", configureUtil.getConfig(Constants.SMS_APPTOKEN));
		headers.add("Cache-Control", "no-cache");
		String result = RestTemplateUtil.post(url, "", headers);

		JSONObject resultJson = JSON.parseObject(result);
		logger.info("url " + url);
		if (resultJson.getIntValue("code") == 1) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public JsonData sendRandom(String mobile, String type) {
		// 校验参数
		if (StringUtils.isBlank(mobile) || StringUtils.isBlank(type)
				|| (!type.equals(SmsType.LOGIN.name()) && !type.equals(SmsType.MOBILE_BIND.name())
						&& !type.equals(SmsType.REGISTER.name()) && !type.equals(SmsType.RESETPWD.name()))
				|| mobile.length() != 11) {
			return JsonData.fail(ErrorCode.INVALID_PARAMETERS);
		}

		User u = userService.findByMobile(mobile);

		if (u != null && (type.equals(SmsType.MOBILE_BIND.name()) || type.equals(SmsType.REGISTER.name()))) {
			return JsonData.fail(ErrorCode.MOBILE_HAS_BINDED);
		}
		if (u == null && (type.equals(SmsType.RESETPWD.name()))) {
			return JsonData.fail(ErrorCode.MOBILE_UNREGISTER);
		}

		// 获取随机验证码
		String code = getRandomCode();
		// code ="000000";
		// 获取下发的短信内容
		String content = "";
		if (type.equals(SmsType.LOGIN.name())) {
			content = configureUtil.getConfig(Constants.LOGIN_CONTENT);
		} else if (type.equals(SmsType.REGISTER.name())) {
			content = configureUtil.getConfig(Constants.REGISTER_CONTENT);
		} else {
			content = configureUtil.getConfig(Constants.MOBILE_BIND_CONTENT);;
		}
		if (StringUtils.isBlank(content)) {
			return JsonData.fail(ErrorCode.INTERNAL_ERROR);
		}
		content = content.replace("{code}", code);
		// 发送短信
		boolean smsResult = sendSms(mobile, content);
		if (smsResult) {
			// 验证码存入redis
			if (type.equals(SmsType.LOGIN.name())) {
				redisUtil.set(RedisUtil.KeyType.Sms_Login, mobile, code, 60 * 10);
			} else if (type.equals(SmsType.MOBILE_BIND.name())) {
				redisUtil.set(RedisUtil.KeyType.Sms_Mobile_Bind, mobile, code, 60 * 10);
			} else if (type.equals(SmsType.REGISTER.name())) {
				redisUtil.set(RedisUtil.KeyType.Sms_Mobile_Register, mobile, code, 60 * 10);
			}
			return JsonData.success();
		} else {
			return JsonData.fail(ErrorCode.SEND_SMS_FAILED);
		}
	}

	// 生成随机六位数
	public String getRandomCode() {
		Random random = new Random();
		String result = "";
		for (int i = 0; i < 6; i++) {
			result += random.nextInt(10);
		}
		return result;
	}
}
