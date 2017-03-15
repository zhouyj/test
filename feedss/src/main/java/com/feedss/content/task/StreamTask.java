package com.feedss.content.task;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.feedss.content.monitor.Listeners;

/**
 * stream状态监听<br>
 * @author wangjingqing
 * @date 02016-08-15
 */
@Component
public class StreamTask {

	Log logger = LogFactory.getLog(getClass());
	
	@Resource(name="streamListeners")
	private Listeners<List<String>> listeners;
	
	@Scheduled(cron="0 0/1 * * * ?")//每1分钟执行一次
	public void work(){
		logger.debug("监控stream状态....");
		/*List<String> streamIds = RoomListeners.streamIds;
		int count = streamIds.size();
		for(int i=0;i<count;i++){
			String streamId = streamIds.get(i);
			listeners.onStart(streamId);//监听stream状态
		}*/
		listeners.onStart("");//监听stream状态
	}
}
