package com.feedss.content.task;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.feedss.content.entity.WatchStream;
import com.feedss.content.entity.WatchStream.WatchType;
import com.feedss.content.service.WatchStreamService;
import com.feedss.user.entity.Offline;
import com.feedss.user.service.OfflineService;

/**
 * 统计观看时长,计算等级<br>
 * @author wangjingqing
 * @date 2016-09-05
 */
@Component
public class WatchTimeTask {

	@Autowired
	private WatchStreamService watchStreamService;
	@Autowired
	private OfflineService offlineService;
	@Scheduled(cron="0 0/1 * * * ?")//每1分钟执行一次
	public void work(){
		 List<WatchStream> watchStreams = watchStreamService.selectNotCalculateWatchStream();
		 if(watchStreams != null && watchStreams.size() > 0){
			 //遍历 WatchStream
			 watchStreams.forEach((watchStream)->{
				 String action = "watch";
				 action = watchStream.getType().equals(WatchType.Stream.name())?"liveStreaming":action;
				 Offline one = offlineService.add(watchStream.getCreator(),action, "stream", watchStream.getStreamId(), watchStream.getWatchTime(), null);
				 if(one!=null){
					 watchStream.setStatus(1);
					 watchStreamService.save(watchStream);
				 }
			 });
		 }
	}
}
