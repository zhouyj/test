package com.feedss.contact;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.feedss.FeedssApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FeedssApplication.class)
@WebAppConfiguration
public class RedisTest {


    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Test
    public void check() throws InterruptedException{
    	String key = "zhouyjtest";
    	String value = "is a test for zhouyj";
    	redisTemplate.opsForValue().set(key, value, 10,TimeUnit.SECONDS);
    	
    	String value_haha = redisTemplate.opsForValue().get(key);
    	Assert.assertEquals(value_haha, value);

    	Thread.sleep(1000*15);
    	
    	value_haha = redisTemplate.opsForValue().get(key);
    	Assert.assertNotEquals(value_haha, value);
    }
}
