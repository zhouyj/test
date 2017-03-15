package com.feedss.content.util;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.feedss.FeedssApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FeedssApplication.class)
@WebAppConfiguration
public class RedisTest {


    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
	private StringRedisTemplate template;
    
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
    
    @Test
    public void testList() {  
        ListOperations<String, String> ops = this.template.opsForList();  
        String key = "name";  
        ops.leftPush(key, "li");  
          
        if (!this.template.hasKey(key)) {
            System.out.println(ops.rightPop(key));  
            ops.leftPush(key, "wang");  
            ops.leftPush(key, "li");  
              
            System.out.println("set succeed");  
        } else {  
            System.out.println("key is exist");  
            Long size = ops.size(key);  
            List<String> list = ops.range(key, 0, size);  
            for (String value : list) {  
                System.out.println(value);  
            }  
        }  
    }  
}
