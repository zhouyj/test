package com.feedss.contact.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.feedss.FeedssApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@SpringApplicationConfiguration(FeedssApplication.class)
public class QcloudServiceTest {


	@Autowired
	QCloudService service;
	
	@Test
	public void testImportAccount() {
		String accountId = "56db561a-3fdc-413a-b542-d0a8810589b8";
		String nickname = "新华炫闻";
		String faceUrl = "http://image.xh.feedss.com/20170306/xinhuaxuanwen.png";
		service.qcloudImportAccount(accountId, nickname, faceUrl);
	}
}
