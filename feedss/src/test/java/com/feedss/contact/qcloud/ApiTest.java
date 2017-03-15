package com.feedss.contact.qcloud;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONObject;
import com.feedss.FeedssApplication;
import com.feedss.base.util.RestTemplateUtil;
import com.feedss.contact.entity.Message;
import com.feedss.contact.qcloud.domain.ImportUserRequestBody;
import com.google.gson.Gson;

import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@SpringApplicationConfiguration(FeedssApplication.class)
public class ApiTest extends TestCase {

	@Autowired
	private QCloudMessageUtil util;
	@Autowired
	private GenerateTLSSignatureUtil generateTLSSignatureUtil;

	@Test
	public void testImportAccount() {
		String accountId = "a1582289-a186-4174-8358-1d833447494b";
		String nickname = "test010";
		String faceUrl = "http://image.feedss.com/20161209/1e314466-088e-4a1a-86f8-8967892e6d86.jpg";
		Object request = new ImportUserRequestBody(accountId, nickname, faceUrl);
		util.importAccount(accountId, nickname, faceUrl);
		String url = "https://console.tim.qq.com/v4/im_open_login_svc/account_import?sdkappid=1400012342&identifier=admin&usersig=eJxFkF1PwjAUQP-LXmek66ibJD6gY0hkIYCLwEvTrd24zn3YVTY0-HdrM*LrObm5594f63W5vWVNA5wyRV3JrYmFrBuDRd*AFJRlSkiNHUIIRuhqT0K2UFdaYOQQB7sI-UvgolKQgRlkvIRqEC3kmkSz*GkRvO*9-kXU-ijvY3sdPH76QZLlEiVFVyyUmNvHQ7Jjo8P9egqzqZq34rh53qvvkHmrJXxkaNvdvak6Tsud12E7xGEUrU4b2T1cl-GCmtv*6se6TleO8SAVlMJwD2GfINcbOEvT*qtSVJ0bYZ5x*QXgn1gh&contenttype=json&random=175453218";
//		String body = JSONObject.toJSONString(request);
//		Gson gson = new Gson();
//		String body = gson.toJson(request);
//		System.out.println("body = " + body);
//		String resp = RestTemplateUtil.postRequest(url, body, null);
//		System.out.println("============resp1 = " + resp);
//		body = "{\"Identifier\":\"a1582289-a186-4174-8358-1d833447494b\",\"Nick\":\"test010\",\"FaceUrl\":\"http://image.feedss.com/20161209/1e314466-088e-4a1a-86f8-8967892e6d86.jpg\"}";
//		resp = RestTemplateUtil.post(url, body, null);
//		System.out.println("============resp2 = " + resp);
		
		
		
	}

	@Test
	public void testSetAppAttr() {
		util.setAppAttrs();
	}

	@Test
	public void testCheckUsersig() {
		String userId = "admin";
		String sig = "eJxFkF1PwjAUQP-LXmek66ibJD6gY0hkIYCLwEvTrd24zn3YVTY0-HdrM*LrObm5594f63W5vWVNA5wyRV3JrYmFrBuDRd*AFJRlSkiNHUIIRuhqT0K2UFdaYOQQB7sI-UvgolKQgRlkvIRqEC3kmkSz*GkRvO*9-kXU-ijvY3sdPH76QZLlEiVFVyyUmNvHQ7Jjo8P9egqzqZq34rh53qvvkHmrJXxkaNvdvak6Tsud12E7xGEUrU4b2T1cl-GCmtv*6se6TleO8SAVlMJwD2GfINcbOEvT*qtSVJ0bYZ5x*QXgn1gh";
		Assert.assertFalse(generateTLSSignatureUtil.needRefreshUserSig(userId, sig));
	}

	@Test
	public void getUuid() {
		for (int i = 0; i < 5; i++)
			System.out.println(UUID.randomUUID());
	}

	@Test
	public void makeObject() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("messageSource", Message.Source.Barrage.name());
		map.put("text", "这是一个弹幕");
		map.put("groupId", "GROUPf3968c57-5f50-4db4-8942-b4c2019810ff");
		Map<String, String> userInfo = new HashMap<String, String>();
		userInfo.put("uuid", "a97d8a1d-800c-437a-ab3b-7a38b4add17b");
		userInfo.put("nickname", "老周test");
		userInfo.put("avatar", "http://image.feedss.com/20160810/e6139273-a802-41c0-b753-82239cd669ef.jpg");
		map.put("userInfo", userInfo);
		String s = JSONObject.toJSONString(map);
		Map<String, String> extMap = new HashMap<String, String>();
		extMap.put("ext", s);
		System.out.println(JSONObject.toJSONString(extMap));
	}
}
