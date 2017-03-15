package com.feedss.content.task;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.feedss.content.monitor.Listeners;
import com.feedss.content.monitor.impl.RoomListeners;

/**
 * 观众状态监听<br>
 * @author wangjingqing
 * @date 02016-08-15
 */
@Component
public class AudienceTask {

	Log logger = LogFactory.getLog(getClass());
	
	@Resource(name="audienceListeners")
	private Listeners<List<String>> listeners;
	
	@Scheduled(cron="0 0/2 * * * ?") //每2分钟执行一次
	public void work(){
		logger.debug("监控room观众在线状态....");
		List<String> streamIds = RoomListeners.streamIds;
		for(int i=0;i<streamIds.size();i++){
			String streamId = streamIds.get(i);
			listeners.onStart(streamId);//监听观众
		}
	}
}
