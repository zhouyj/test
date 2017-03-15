package com.feedss.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONObject;
import com.feedss.FeedssApplication;
import com.feedss.user.entity.Log;
import com.feedss.user.service.LogService;

/**
 * Created by qin.qiang on 2016/8/2 0002.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@SpringApplicationConfiguration(FeedssApplication.class)
public class LogTest {

    @Autowired
    LogService logService;
    
    @org.junit.Test
    public void test(){
    	List<Log> logs = new ArrayList<Log>();
    	Log l = new Log();
    	l.setAction("testAction");
    	l.setAppChanel("iOS");
    	l.setAppVersion("3");
    	l.setCreated(new Date());
    	l.setDeviceOS("0");
    	l.setDeviceType("0");
    	l.setNetwork("WiFi");
    	l.setObject("2");
    	l.setObjectId("testobj1");
    	l.setType("log");
    	l.setUserId("testUserId001");
    	logs.add(l);
    	
    	Log l2 = new Log();
    	l2.setAction("testAction2");
    	l2.setAppChanel("iOS");
    	l2.setAppVersion("3");
    	l2.setCreated(new Date());
    	l2.setDeviceOS("0");
    	l2.setDeviceType("0");
    	l2.setNetwork("4G");
    	l2.setObject("2");
    	l2.setObjectId("testobj2");
    	l2.setType("log");
    	l2.setUserId("testUserId002");
    	logs.add(l2);
    	String s = JSONObject.toJSONString(logs);
    	System.out.println(s);
//    	logService.add(logs);
    }
    

    @Test
	private void testA() {
		File file = new File("/Users/zhouyujuan/Desktop/1.txt");
		StringBuffer body = new StringBuffer();
		if(file.exists()){
	        BufferedReader reader = null;
	        try {
	            reader = new BufferedReader(new FileReader(file));
	            String tempString = null;
	            while ((tempString = reader.readLine()) != null) {
	            	body.append(tempString);
	            }
	            reader.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                }
	            }
	        }
		}
		String s = body.toString();
		JSONObject json=JSONObject.parseObject(s);

	}
}
