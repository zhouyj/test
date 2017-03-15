package com.feedss.contact;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.feedss.FeedssApplication;
import com.feedss.base.util.conf.ConfigureUtil;

/**
 * Created by shenbingtao on 2016/8/2.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FeedssApplication.class)
@WebAppConfiguration
public class ConfigureTest {

    @Autowired
    private ConfigureUtil configureUtil;

    @Test
    public void testAdd(){
        configureUtil.save("hello", "world123");
    }

    @Test
    public void testGet(){
        System.out.println(configureUtil.getConfig("hello"));
    }

}
