package com.feedss.content.task;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.feedss.content.monitor.Listeners;
import com.feedss.content.monitor.impl.RoomListeners;
import com.feedss.content.service.StreamService;

/**
 * 监控room状态<br>
 * @author wangjingqing
 * @date 02016-08-06
 */
@Component
public class RoomTask {

	Log logger = LogFactory.getLog(getClass());
	
	@Resource(name="roomListeners")
	private Listeners<List<String>> listeners;
	
	@Autowired
	private StreamService streamService;
	
	//TODO 时间待定
	@Scheduled(cron="0 0/3 * * * ?")//每3分钟执行一次
	public void work(){
		logger.debug("监控Room在线状态....");
		List<String> closeSrteamIds = new ArrayList<String>();
		List<String> streamIds = RoomListeners.streamIds;
		for(int i=0;i<streamIds.size();i++){
			String streamId = streamIds.get(i);
			closeSrteamIds = listeners.onStart(streamId);//获取监听数据
			if(closeSrteamIds ==null){
				continue;
			}
			if(closeSrteamIds.contains(streamId)){
				RoomListeners.deleteListener(streamId);//删除监听
			}
		}
		if(closeSrteamIds != null && closeSrteamIds.size() > 0){
			logger.info("异常视频UUID:"+closeSrteamIds.toString());
			try {
				streamService.closeStreams(closeSrteamIds);
			} catch (Exception e) {
				logger.info("关闭视频异常："+closeSrteamIds.toString());
			}
		}
	}
}
