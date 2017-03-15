package com.feedss.user.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.JsonResponse;
import com.feedss.user.service.SmsService;

/**
 * Created by qinqiang on 2016/7/29.
 * 接口
 */

@RestController
@RequestMapping("user")
public class SmsController {

	@Autowired
	SmsService smsService;
	
	public enum SmsType{
		LOGIN, MOBILE_BIND, REGISTER, RESETPWD
	}
	
	/**
	 * 接口6.2发送短信验证码
	 * //@param mobile
	 * //@param type
	 * @return
	 */
	@RequestMapping("sendVerifyCode")
	public ResponseEntity<Object> sendVerifyCode(HttpServletRequest request,@RequestBody String body, HttpSession session){
		
		String strJson=body;
		JSONObject jsonObject = JSON.parseObject(strJson);
		String mobile = jsonObject.getString("mobile");
		String type = jsonObject.getString("type");
		return JsonResponse.response(smsService.sendRandom(mobile, type));
	}
    
}
