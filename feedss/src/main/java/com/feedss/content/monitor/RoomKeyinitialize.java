package com.feedss.content.monitor;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.feedss.content.monitor.impl.RoomListeners;

/**
 * 在初始化检查redis是否有room的key,有放进#RoomListeners.addStreamId(String id)，开始监控room<br>
 * @author wangjingqing
 * @date 2016-08-22
 */
@Component("roomKeyinitialize")
public class RoomKeyinitialize extends Thread{

	Log logger = LogFactory.getLog(getClass());
			
	@Autowired
	private StringRedisTemplate template;
	
	//直播key
	private static String ROOM_KEY = "room_key_";
	
	@Override
	public void run() {
		Set<String> keys = template.keys(ROOM_KEY+"*");//查询所有的room key
		for(String key : keys){
			String streamId = key.replace(ROOM_KEY, "");
			RoomListeners.addStreamId(streamId);
		}
	}
}
