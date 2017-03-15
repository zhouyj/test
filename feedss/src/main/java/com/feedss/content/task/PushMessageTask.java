package com.feedss.content.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.feedss.contact.entity.Message;
import com.feedss.contact.service.MessageService;
import com.feedss.content.entity.Trailer;
import com.feedss.content.entity.UserTrailer;
import com.feedss.content.model.room.Room;
import com.feedss.content.model.room.RoomUser;
import com.feedss.content.service.RoomService;
import com.feedss.content.service.StreamService;
import com.feedss.content.service.TrailerService;
import com.feedss.user.entity.UserRelation.RelationType;
import com.feedss.user.model.UserRelationVO;
import com.feedss.user.service.UserRelationService;

/**
 * 创建直播下发消息到关注的用户<br>
 * 
 * @author wangjingqing
 * @date 2016-08-22
 */
@Component
public class PushMessageTask {

	Log logger = LogFactory.getLog(getClass());

	@Autowired
	private RoomService roomService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private UserRelationService userRelationService;
	@Autowired
	private StreamService streamService;
	@Autowired
	private TrailerService trailerService;

	@Async
	public void push(final String streamId) {
		logger.debug("try to push room message, streamId = " + streamId);
		try {
			Room room = roomService.getRoom(streamId);
			if (room == null) {
				logger.info("push related room failed, ->" + streamId + " room信息不存在");
				return;
			}
			RoomUser roomUser = room.getRoomUser();
			String userId = roomUser.getUuid();
//			Set<String> userIds = userService.followUser(userId);
			List<String> userIds = new ArrayList<>();
			List<UserRelationVO> list=userRelationService.userFollowBy(userId, userId, 
					RelationType.FOLLOW.toString(), "", 500);
			if(list!=null){
				for(UserRelationVO ur:list){
					userIds.add(ur.getFromUserId());
				}
			}
			if (userIds.size() > 0) {
				String nickName = roomUser.getNickName();
				// 发送的信息
				Map<String, Object> extInfo = new HashMap<String, Object>();
				extInfo.put("streamInfo", streamService.getStreamInfo(streamId));
				extInfo.put("messageSource", Message.Source.StreamCreate.name());
//				pushService.message(userIds, nickName + "正在直播",
//						JSONObject.toJSONString(extInfo));
				String[] array =new String[userIds.size()];
				userIds.toArray(array);
				messageService.sendSystemMessage(nickName + "正在直播", extInfo, array);
			}
		} catch (Exception e) {
			logger.error("ROOM信息推送失败:streamId=" + streamId, e);
		}
	}
	
	@Async
	public void pushToSubber(Trailer trailer){
		logger.info("to trailer, trailer = " + JSONObject.toJSONString(trailer));
		List<UserTrailer> userList = trailerService.selectUserTrailer(trailer.getUuid());
		if(userList == null || userList.size() < 1){//判断是否有预约用户
			return;
		}
		//发送的信息
		Map<String,Object> extInfo = new HashMap<String,Object>();
		extInfo.put("messageSource", Message.Source.AppointmentReminder);
		extInfo.put("streamInfo", streamService.getStreamInfo(trailer.getStreamId()));
		
		List<String> users = new ArrayList<String>();
		int size = userList.size();
		for (int i=0;i < size; i++) {
			users.add(userList.get(i).getUserId());
			if(users.size() == 500 || size == i+1){// 判断预约人数是否大于 500，通知消息一次最多发送500个用户
				String[] array =new String[users.size()];
				users.toArray(array);
				messageService.sendSystemMessage("您预约的直播开始了,快来看吧", extInfo, array);
				users.clear();
			}
		}
	}
}
