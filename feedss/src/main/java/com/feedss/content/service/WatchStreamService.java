package com.feedss.content.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.feedss.base.Pages;
import com.feedss.content.entity.WatchStream;
import com.feedss.content.repository.WatchStreamRepository;

/**
 * 观看时长<br>
 * @author wangjingqing
 * @date 2016-08-24
 */
@Component
public class WatchStreamService {

	@Autowired
	private WatchStreamRepository watchStreamRepository;
	
	/**
	 * 查询排行榜<br>
	 */
	public Pages<WatchStream> selectRanking(Date startTime,String type, Integer pageNo,Integer pageSize){
		startTime = startTime== null ?new Date():startTime;
		Integer count = watchStreamRepository.selectWatchRankingCount(startTime,type);
		List<WatchStream> pages = new ArrayList<WatchStream>();
		if(count.intValue() > 0){
			pageNo = pageNo == null || pageNo.intValue() < 1 ? 1:pageNo;
			pages = watchStreamRepository.selectWatchRanking(startTime,type,(pageNo-1) * pageSize,pageSize);
		}
		return new Pages<WatchStream>(count, pages);
	}
	
	/**
	 * 报错观看时长和直播时长<br>
	 * @param userId
	 * @param watchTime
	 * @return
	 */
	public WatchStream save(String userId,String streamId,long watchTime,String type){
		WatchStream watchStream = new WatchStream();
		watchStream.setCreated(new Date());
		watchStream.setWatchTime(watchTime);
		watchStream.setType(type);
		watchStream.setStreamId(streamId);
		watchStream.setCreator(userId);
		watchStream = watchStreamRepository.save(watchStream);
		return watchStream;
	}
	
	/**
	 * 查询未统计的信息<br>
	 * @return List<WatchStream>
	 */
	public List<WatchStream> selectNotCalculateWatchStream(){
		return watchStreamRepository.findByStatus(0);
	}
	
	/**
	 * 保存<br>
	 * @param watchStream
	 * @return
	 */
	public WatchStream save(WatchStream watchStream){
		return watchStreamRepository.save(watchStream);
	}
}
