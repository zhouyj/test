package com.feedss.content.monitor.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.feedss.content.model.room.Room;
import com.feedss.content.model.room.RoomUser;
import com.feedss.content.monitor.Listeners;
import com.feedss.content.service.ChatRoomService;
import com.feedss.content.service.RoomService;

/**
 * 更新在线观众信息<br>
 * @author wangjingqing
 *
 */
@Component(value="audienceListeners")
public class AudienceListeners implements Listeners<List<String>>{

	Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private RoomService roomService;
	
	@Autowired
	private ChatRoomService chatRoomService;
	@Override
	public List<String> onStart(String streamId) {
		//step:1 获取直播房间
		Room room = roomService.getRoom(streamId);
		if(room == null){
			logger.info("主播房间：["+streamId+"]不存在");
			return null;
		}
		String groupId = room.getGroupId();//获取分组ID
		List<Map<String, Object>> list = chatRoomService.getAudienceList("", groupId,"",500);//获取观众用户信息
		for (Map<String, Object> info:list) {
			//Integer isShutUp = ConvertUtil.objectToInt(info.get("isShutUp"), false);
			String userId = info.get("userId").toString();
			if(StringUtils.isNotBlank(userId)){//在线
				
				//step:1 更新观众
				RoomUser roomUser = roomService.getAudience(streamId, userId);
				if(roomUser != null){//更新房间时间
					roomUser.setUpdateTiem(new Date());
					roomService.addAudience(roomUser);
				}
				//step：2 更新请求者
				RoomUser requestRoom = roomService.getRequestConnect(streamId, userId);
				if(requestRoom != null){
					requestRoom.setUpdateTiem(new Date());
					roomService.addRequestConnect(requestRoom);
				}
				//step:3 更新连接
				Room childRoom = roomService.getChildRoom(streamId, userId);
				if(childRoom != null){
					childRoom.setUpdateTime(new Date());
					roomService.addChildRoom(childRoom);
				}
			}
			/*else{//离线
				roomService.removeAudience(streamId, userId);//移除观众
				roomService.removeRequestConnect(streamId, userId);//移除请求
				roomService.removeChildRoom(userId, streamId);//移除连接
			}*/
		}
		return null;
	}

}
