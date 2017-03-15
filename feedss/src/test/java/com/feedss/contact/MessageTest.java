package com.feedss.contact;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.feedss.FeedssApplication;
import com.feedss.contact.entity.CustomMessageContent;
import com.feedss.contact.entity.Message;
import com.feedss.contact.entity.MessageContent;
import com.feedss.contact.entity.TextMessageContent;
import com.feedss.contact.service.MessageService;
import com.google.gson.Gson;

/**
 * Created by shenbingtao on 2016/8/2.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FeedssApplication.class)
@WebAppConfiguration
public class MessageTest {

    private static SerializeConfig mapping = new SerializeConfig();
    private static String dateFormat;
    static {
        dateFormat = "yyyy-MM-dd HH:mm:ss";
        mapping.put(Date.class, new SimpleDateFormatSerializer(dateFormat));
    }

    @Autowired
    private MessageService messageService;


	@Test
	public void testJsonParse(){
		Message m = new Message();
		m.setFromAccount("laozhou");
		String[] toAccount = new String[]{"00beae0e-7ae6-4953-81a4-6cc26bb4fbc8"};
		m.setToAccount(toAccount);
		List<MessageContent> list = new ArrayList<MessageContent>();
		TextMessageContent c1 = new TextMessageContent();
		c1.setText("您预约的直播快到了,快来看吧");
		list.add(c1);
		
		CustomMessageContent c3 = new CustomMessageContent();
//		c3.setData("这是data，非必需");
//		c3.setDesc("这是描述，非必需");
		Map<String, Object> streamMap = new HashMap<String, Object>();
		streamMap.put("uuid", "15d1e145-88a9-49f3-81ff-1b4d4aa32844");
		streamMap.put("title", "测试");
		streamMap.put("cover", "http://image.feedss.com/20160905/0a8978b3-6621-4a81-b98a-babf8d773de2.jpg");
		streamMap.put("started", "2016-09-05");
		streamMap.put("isOnline", 1);
		streamMap.put("playUri", "rtmp://live.feedss.com/lvshang/15d1e145-88a9-49f3-81ff-1b4d4aa32844");
		streamMap.put("isSecret", 0);
		streamMap.put("isVR", 0);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("messageSource", "AppointmentReminder");
		map.put("streamInfo", streamMap);
		c3.setExt(JSONObject.toJSONString(map));
		list.add(c3);
		
		m.setMsgBody(list);
		
		Gson json = messageService.getMessageContentGson();
		
		String messageStr = json.toJson(m, Message.class);
		
		System.out.println(messageStr);
		
		Message other = json.fromJson(messageStr, Message.class);
		System.out.println(json.toJson(other));
	}
	
	public void testStreamMessage(){
		
	}
	
}
