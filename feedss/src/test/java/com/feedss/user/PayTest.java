package com.feedss.user;

import com.feedss.FeedssApplication;
import com.feedss.user.entity.Account;
import com.feedss.user.repository.AccountRepository;
import com.feedss.user.service.WeChatService;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by qin.qiang on 2016/8/2 0002.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@SpringApplicationConfiguration(FeedssApplication.class)
public class PayTest {

    @Autowired
    WeChatService weChatService;
    @org.junit.Test
    public void test(){
       // TreeMap<String,String> map = weChatService.unifiedOrder("12345678",800,"61.49.247.186");
       /* for(String key:map.keySet()){
            System.out.println(key+": "+map.get(key));
        }*/
       Map<String,String> map =  weChatService.queryOrder("12345678");
        for(String key:map.keySet()){
            System.out.println(key+": "+map.get(key));
        }
    }
}
