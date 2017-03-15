package com.feedss.user;

import com.feedss.FeedssApplication;
import com.feedss.base.util.RedisUtil;
import com.feedss.base.util.RedisUtil.KeyType;
import com.feedss.user.service.SmsService;

import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.SystemProfileValueSource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.DigestUtils;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)  
@Configuration
@SpringApplicationConfiguration(FeedssApplication.class)
public class SmsTest extends TestCase{

	@Autowired
	SmsService smsService;
	
	
	@org.junit.Test
	public void test(){
		System.out.println(smsService.sendSms("18801291630","【秦强】1989"));
	}
}
