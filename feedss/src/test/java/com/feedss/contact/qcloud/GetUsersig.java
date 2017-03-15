package com.feedss.contact.qcloud;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.feedss.FeedssApplication;
import com.feedss.contact.qcloud.GenerateTLSSignatureUtil;

@RunWith(SpringJUnit4ClassRunner.class)  
@Configuration
@SpringApplicationConfiguration(FeedssApplication.class)
public class GetUsersig {

	@Autowired
	private GenerateTLSSignatureUtil generateTLSSignatureUtil;
	
	public static String getUUID(){
		return UUID.randomUUID().toString();
	}
	
	@Test
	public void getUserSig(){
		String uuid = "00653ef7-b6af-49fb-b64e-30f5c263ade4";
		String userSig = generateTLSSignatureUtil.getUserSig(uuid);
		boolean need = generateTLSSignatureUtil.needRefreshUserSig(uuid, userSig);
		Assert.assertFalse(need);
	}
}
