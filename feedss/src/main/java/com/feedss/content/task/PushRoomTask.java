package com.feedss.content.task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.feedss.contact.entity.Message;
import com.feedss.contact.service.MessageService;
import com.feedss.content.entity.Stream.StreamStatus;
import com.feedss.content.model.room.Room;
import com.feedss.content.model.room.RoomUser;
import com.feedss.content.service.ReleaseProductService;
import com.feedss.content.service.RoomService;
import com.feedss.portal.service.TaskService;

/**
 * 监控room信息变化推送<br>
 * 
 * @author wangjingqing
 * @date 2016-08-22
 */
@Component
public class PushRoomTask extends Thread {

	Log logger = LogFactory.getLog(getClass());

	@Autowired
	private RoomService roomService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private ReleaseProductService releaseProductService;

	@Scheduled(cron = "0/5 * * * * ?")//每5秒钟执行一次
	public void work() {
		
		while (true) {
			Set<String> streamIdSet = new HashSet<String>();
			for(int i=0;i<100;i++){
				String streamId = roomService.PopRoom();
				if(StringUtils.isEmpty(streamId)) break;
				streamIdSet.add(streamId);
			}
			if(streamIdSet.isEmpty()){
				break;				
			}else{
				logger.info("push room task, set size is " + streamIdSet.size());
				for(String streamId:streamIdSet){
					if (streamId != null) {
						Room room = roomService.getRoom(streamId);
						if (room == null) {
							logger.info("streamId:" + streamId + " room信息不存在");
							continue;
						}
						RoomUser roomUser = room.getRoomUser();

						Map<String, Object> extInfo = new HashMap<String, Object>();// 扩展信息
						extInfo.put("groupId", room.getGroupId());
						Map<String, Object> roomMap = roomService.getRoomInfo(streamId);
						if (roomMap == null)
							roomMap = new HashMap<String, Object>();

						Integer releaseCount = releaseProductService.selectCount(streamId);// 发布商品数量
						int robCount = taskService.getRobCount(roomUser.getUuid(), streamId);// 任务数量
						roomMap.put("isReleasePro", releaseCount.intValue() > 0 ? 1 : 0);
						if (room.getStatus() == StreamStatus.Publishing.ordinal()) {
							roomMap.put("isRob", robCount > 0 ? 1 : 0);
							extInfo.put("isRob", robCount > 0 ? 1 : 0);
						}
						extInfo.put("room", roomMap);
						logger.info("推送roomInfo->" + JSONObject.toJSONString(roomMap));// 监控数据（Test）
						extInfo.put("messageSource", Message.Source.RoomInfo.name());
						messageService.sendGroupMessage(room.getGroupId(), "", extInfo, null);
					}
				}
			}
		}
	}
}
