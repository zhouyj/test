package com.feedss.user;

import com.feedss.FeedssApplication;
import com.feedss.base.util.RedisUtil;
import com.feedss.base.util.RedisUtil.KeyType;
import com.feedss.user.model.UserVo;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)  
@Configuration
@SpringApplicationConfiguration(FeedssApplication.class)
public class RedisTest extends TestCase{

	@Autowired
	RedisUtil redisUtil;
	

	@SuppressWarnings("rawtypes")
    @Autowired
    private RedisTemplate redisTemplate; 
	
	@org.junit.Test
	public void testGetAndSet1(){
		String userId = "2f92aaf6-85d9-4605-8e5c-b549ba2380ea";
		redisUtil.addToSet(KeyType.TempUserList, "_1", "1","2");
		System.out.println(redisUtil.setTime(KeyType.TempUserList, "_1", 60));
		long i=redisUtil.getTime(KeyType.TempUserList, "_1");
		System.out.println(i);
	    
	}
	
	@Test
	public void testZSet(){
		
		for(int i=0;i<100; i++){
			long j = new Random().nextInt(50);
			UserVo u = new UserVo();
			u.setFollowByCount(j);
			u.setUuid("uuid" + j);
			redisTemplate.opsForZSet().add("laozhoutest", u, u.getFollowByCount());
		}
		
		
		System.out.println("count = " + redisTemplate.opsForZSet().count("laozhoutest", 0, 10));
		System.out.println("range by score: " + redisTemplate.opsForZSet().reverseRangeByScore("laozhoutest", 0, 10));		

		
	}
	
//	@org.junit.Test
//	public void testGetAndSet(){
//		String userId = "2f92aaf6-85d9-4605-8e5c-b549ba2380ea";
//		byte[] b =  (new Date() + DigestUtils.md5DigestAsHex(userId.getBytes())).getBytes();  
//	    String token = DigestUtils.md5DigestAsHex(b); 
//
//		System.out.println(token);
//	    redisUtil.set(KeyType.TOKEN, userId, token, 5184000);
//	    System.out.println("get token = " + redisUtil.get(KeyType.TOKEN, userId));
//	    
//	    
//	}
}
