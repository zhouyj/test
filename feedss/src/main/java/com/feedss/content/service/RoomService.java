package com.feedss.content.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.Constants;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonData;
import com.feedss.base.util.CommonUtil;
import com.feedss.base.util.ConvertUtil;
import com.feedss.base.util.DateUtil;
import com.feedss.base.util.MD5Util;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.contact.entity.Message;
import com.feedss.contact.service.MessageService;
import com.feedss.content.entity.Category;
import com.feedss.content.entity.Stream;
import com.feedss.content.entity.Stream.StreamType;
import com.feedss.content.entity.WatchStream.WatchType;
import com.feedss.content.model.room.Room;
import com.feedss.content.model.room.RoomUser;
import com.feedss.content.monitor.impl.RoomListeners;
import com.feedss.portal.entity.Interaction.InteractionType;
import com.feedss.portal.entity.PushStreamTerminal;
import com.feedss.portal.service.InteractionService;
import com.feedss.portal.service.TerminalService;
import com.feedss.user.entity.Role;
import com.feedss.user.model.UserProfileVo;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.UserService;

/**
 * 直播间服务<br>
 * 
 * @author wangjingqing
 *
 */
@Component
public class RoomService {

	Log logger = LogFactory.getLog(getClass());

	// 直播key
	private static String ROOM_KEY = "room_key_";
	// 观众key
	private static String ROOM_AUDIENCE_KEY = "room_audience_key_";
	// 连接key
	private static String ROOM_CHILD_KEY = "room_child_key_";
	// 请求连接的
	private static String ROOM_REQUEST_CONNECT_KEY = "room_request_connect_key";
	// 队列key
	private static String ROOM_PUSH_KEY = "room_push_key";
	// 创建直播下发消息key
	private static String CREATE_ROOM_PUSH_KEY = "create_room_push_key";

	private static Integer ROOM_CHACHE_TIME = 60 * 60 * 24 * 7;
		
	@Autowired
	private StringRedisTemplate template;
	@Autowired
	private UserService userService;
	@Autowired
	private ChatRoomService chatRoomService;
	@Autowired
	private StreamService streamService;
	@Autowired
	private WatchStreamService watchStreamService;
	@Autowired
	private ConfigureUtil configureUtil;
	@Autowired
	private InteractionService interactionService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private TerminalService terminalService;
	@Autowired
	private SrsService srsService;

	public Stream createStream(String categoryId, String userId, String title, String cover, String secret,
			double longtitude, double latitude, String position, String playbackUri, String bannerId,
			String durationStr, String terminalPublishUrl, StreamType streamType) {
		long start = System.currentTimeMillis();
		// step:1 创建直播
		Category ctg = categoryService.selectCategory(categoryId);

		if(streamType==null){
			if (ctg != null && ctg.getName().toLowerCase().contains("vr")) {
				streamType = StreamType.VR;
			}else {
				streamType = StreamType.Normal;
			}
		}
		
		Long duration = 0l;
		if (!StringUtils.isEmpty(durationStr)) {
			try {
				duration = Long.parseLong(durationStr);
			} catch (Exception e) {
				logger.error("解析时长出错", e);
			}
		}
		String clearSecret = secret;
		if (StringUtils.isNotBlank(secret)) {
			secret = MD5Util.MD5String(secret);
		}
		long step1 = System.currentTimeMillis();
		Stream stream = streamService.createStream(userId, title, categoryId, cover, clearSecret, secret, longtitude,
				latitude, position, streamType, playbackUri, bannerId, duration, terminalPublishUrl);
		
		long step2 = System.currentTimeMillis();
		// step:2 创建房间信息
		Room room = createRoom(stream, CommonUtil.getGroupId(stream.getUuid()), 1);
		if (room == null) {// 创建失败
			stream.setDeleted(true);// 删除创建的直播
			streamService.save(stream);
			return null;
		}
		long step3 = System.currentTimeMillis();
		// step:3 添加房间
		createRoom(room);
		// step:4 添加监听
		RoomListeners.addStreamId(room.getUuid());
		long step4 = System.currentTimeMillis();

		// step:6 创建聊天室
		chatRoomService.createChatRoom(userId, room.getGroupId(), stream.getAppName());
		long step5 = System.currentTimeMillis();
		if (logger.isDebugEnabled()) {
			logger.debug("createStream cost time, " + (step1 - start) + ", " + (step2 - step1) + ", " + (step3 - step2)
					+ ", " + (step4 - step3) + ", " + (step5 - step4));
		}
		return stream;
	}

	/**
	 * 创建直播间<br>
	 * 
	 * <pre>
	 * 在redis中创建直播信息，以room_user_key_和userId作为key，把room转换JSON<code>JSONObject</code>作为value
	 * 缓存时间 60 * 60 * 24 * 7秒。
	 * 
	 * <pre>
	 * 
	 * @param room
	 *            房间信息
	 */
	public void createRoom(Room room) {
		ValueOperations<String, String> strRedis = template.opsForValue();
		strRedis.set(ROOM_KEY + room.getUuid(), JSONObject.toJSONString(room), ROOM_CHACHE_TIME, TimeUnit.SECONDS);
	}

	/**
	 * 获取room<br>
	 * 
	 * @param uuid
	 * @return Room
	 */
	public Room getRoom(String uuid) {
		ValueOperations<String, String> strRedis = template.opsForValue();
		String roomStr = strRedis.get(ROOM_KEY + uuid);
		if (roomStr == null) {
			return null;
		}
		return JSONObject.parseObject(roomStr, Room.class);
	}

	/**
	 * 删除room<br>
	 * 
	 * @param uuid
	 * @return Room
	 */
	public void removeRoom(String uuid) {
		template.delete(ROOM_KEY + uuid);
	}

	/**
	 * 增加子类room<br>
	 * 
	 * @param room
	 */
	public void insertChildRoom(Room room) {
		ValueOperations<String, String> strRedis = template.opsForValue();
		String roomKey = ROOM_KEY + room.getParentId();
		String roomStr = strRedis.get(roomKey);
		Room parentRoom = JSONObject.parseObject(roomStr, Room.class);
		List<Room> childList = parentRoom.getChildRoom();
		childList.add(room);
		parentRoom.setChildRoom(childList);
		strRedis.set(roomKey, JSONObject.toJSONString(parentRoom), ROOM_CHACHE_TIME, TimeUnit.SECONDS);
	}

	/**
	 * 添加连接<br>
	 * 
	 * @param room
	 */
	public void addChildRoom(Room room) {
		HashOperations<String, Object, Object> hashRedis = template.opsForHash();
		hashRedis.put(ROOM_CHILD_KEY + room.getParentId(), room.getRoomUser().getUuid(), JSONObject.toJSONString(room));
	}

	/**
	 * 获取连接<br>
	 * 
	 * @param uuid
	 * @param parentId
	 * @return Room
	 */
	public Room getChildRoom(String userId, String parentId) {
		HashOperations<String, Object, Object> hashRedis = template.opsForHash();
		Object obj = hashRedis.get(ROOM_CHILD_KEY + parentId, userId);
		if (obj == null) {
			return null;
		}
		return JSONObject.parseObject(obj.toString(), Room.class);
	}

	/**
	 * 获取所有连接<br>
	 * 
	 * @param uuid
	 * @param parentId
	 * @return List<Room>
	 */
	public List<Room> getChildRoomList(String parentId) {
		HashOperations<String, Object, Object> hashRedis = template.opsForHash();
		Map<Object, Object> obj = hashRedis.entries(ROOM_CHILD_KEY + parentId);
		List<Room> childList = new ArrayList<Room>();
		if (obj == null) {
			return childList;
		}
		Iterator<Entry<Object, Object>> iter = obj.entrySet().iterator();
		while (iter.hasNext()) {
			childList.add(JSONObject.parseObject(iter.next().getValue().toString(), Room.class));
		}
		return childList;
	}

	/**
	 * 移除指定连接<br>
	 * 
	 * @param uuid
	 * @param parentId
	 */
	public void removeChildRoom(String userId, String parentId) {
		HashOperations<String, Object, Object> hashRedis = template.opsForHash();
		hashRedis.delete(ROOM_CHILD_KEY + parentId, userId);
	}

	/**
	 * 移除所有连接<br>
	 * 
	 * @param uuid
	 * @param parentId
	 */
	public void removeChildRoom(String parentId) {
		template.delete(ROOM_CHILD_KEY + parentId);
	}

	/**
	 * 添加观众<br>
	 * 
	 * @param user
	 */
	@Async
	public void addAudience(RoomUser user) {
		HashOperations<String, Object, Object> hashRedis = template.opsForHash();
		hashRedis.put(ROOM_AUDIENCE_KEY + user.getStreamId(), user.getUuid(), JSONObject.toJSONString(user));
	}

	/**
	 * 更新观众与主播的关注关系
	 * 
	 * @param streamId
	 * @param userId
	 * @param follow
	 */
	public void updateAudienceRelation(String streamUserId, String userId, int follow) {
		if (StringUtils.isEmpty(streamUserId))
			return;
		List<Stream> streamList = streamService.selectOnLine(streamUserId);
		streamList.forEach((stream) -> {
			RoomUser roomUser = getAudience(stream.getUuid(), userId);
			if (roomUser != null) {
				roomUser.setIsFollow(follow);
				addAudience(roomUser);
			}
		});

	}

	/**
	 * 获取观众<br>
	 * 
	 * @return
	 */
	public RoomUser getAudience(String streamId, String userId) {
		HashOperations<String, Object, Object> hashRedis = template.opsForHash();
		Object obj = hashRedis.get(ROOM_AUDIENCE_KEY + streamId, userId);
		if (obj == null) {
			return null;
		}
		return JSONObject.parseObject(obj.toString(), RoomUser.class);
	}

	/**
	 * 获取所有观众<br>
	 * 
	 * @param streamId
	 * @return List<RoomUser>
	 */
	public List<RoomUser> getAudienceAll(String streamId) {
		HashOperations<String, Object, Object> hashRedis = template.opsForHash();
		Map<Object, Object> obj = hashRedis.entries(ROOM_AUDIENCE_KEY + streamId);
		List<RoomUser> audiences = new ArrayList<RoomUser>();
		if (obj == null) {
			return audiences;
		}
		Iterator<Entry<Object, Object>> iter = obj.entrySet().iterator();
		while (iter.hasNext()) {
			audiences.add(JSONObject.parseObject(iter.next().getValue().toString(), RoomUser.class));
		}
		return audiences;
	}

	/**
	 * 重新创建房间<br>
	 * 
	 * @param userId
	 * @param groupId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public List<RoomUser> selectAudienceAll(String userId, String groupId, String streamId, String cursorId,
			Integer pageSize) {
		List<RoomUser> roomUserList = new ArrayList<RoomUser>();
		List<Map<String, Object>> list = chatRoomService.getAudienceList(userId, groupId, cursorId, pageSize);
		for (Map<String, Object> info : list) {
			RoomUser roomUser = new RoomUser();
			roomUser = this.getRoomUser(ConvertUtil.objectToString(info.get("userId"), false));
			roomUserList.add(roomUser);
		}
		return roomUserList;
	}

	/**
	 * 重新创建房间<br>
	 * 
	 * @param userId
	 * @param groupId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public Room newRoom(String streamId) {
		Stream stream = streamService.selectStream(streamId);
		if (stream == null)
			return null;
		// step:1 创建room
		Room room = createRoom(stream, CommonUtil.getGroupId(streamId), 1);
		// step:2 添加room到缓存中
		createRoom(room);
		return room;
	}

	public Map<String, Object> getBasicRoomInfo(String streamId) {
		Map<String, Object> reMap = new HashMap<String, Object>();
		// step:1 添加房间信息
		Room room = getRoom(streamId);
		if (room == null) {
			room = newRoom(streamId);
		}
		reMap.put("uuid", room.getUuid());
		reMap.put("groupId", room.getGroupId());
		reMap.put("onLineCount", room.getOnLineCount());
		reMap.put("praiseCount", room.getPraiseCount());
		reMap.put("status", room.getStatus());
		reMap.put("user", roomUserToMap(getRoomUser(room.getRoomUser().getUuid())));
		reMap.put("bannerId", room.getBannerId());
		reMap.put("title", room.getName());
		return reMap;
	}

	/**
	 * 获取房间信息<br>
	 * 
	 * @param streamId
	 * @return Map<String,Object>
	 */
	public Map<String, Object> getRoomInfo(String streamId) {
		Map<String, Object> reMap = new HashMap<String, Object>();
		// step:1 添加房间信息
		Room room = getRoom(streamId);
		if (room == null) {
			return reMap;
		}
		reMap.put("uuid", room.getUuid());
		reMap.put("groupId", room.getGroupId());
		reMap.put("onLineCount", room.getOnLineCount());
		reMap.put("praiseCount", room.getPraiseCount());
		reMap.put("status", room.getStatus());

		reMap.put("user", roomUserToMap(getRoomUser(room.getRoomUser().getUuid())));
		String userId = room.getRoomUser().getUuid();// 主播id
		// step:2 添加观众
		List<RoomUser> audienceList = selectAudienceAll(userId, room.getGroupId(), streamId, "", 20);// getAudienceAll(streamId);
		List<Map<String, Object>> audiences = new ArrayList<Map<String, Object>>();
		for (RoomUser roomUser : audienceList) {
			if (roomUser.getUuid().equals(userId)) {// 过滤主播信息
				continue;
			}
			audiences.add(roomUserToMap(roomUser));
		}
		reMap.put("listUser", audiences);

		// step:3 添加连接
		List<Room> childs = getChildRoomList(streamId);
		List<Map<String, Object>> childList = new ArrayList<Map<String, Object>>();
		for (Room childRoom : childs) {
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			tmpMap.put("uuid", childRoom.getUuid());
			tmpMap.put("userId", childRoom.getRoomUser().getUuid());
			tmpMap.put("parentId", ConvertUtil.objectToString(childRoom.getParentId(), false));
			tmpMap.put("playUri", ConvertUtil.objectToString(childRoom.getPlayUri(), false));
			tmpMap.put("publishUri", ConvertUtil.objectToString(childRoom.getPublishUri(), false));
			childList.add(tmpMap);
		}
		reMap.put("childStreams", childList);
		reMap.put("bannerId", room.getBannerId());
		return reMap;
	}

	/**
	 * 移除指定观众<br>
	 * 
	 * @param streamId
	 * @param userId
	 */
	public void removeAudience(String streamId, String userId) {
		HashOperations<String, Object, Object> hashRedis = template.opsForHash();
		hashRedis.delete(ROOM_AUDIENCE_KEY + streamId, userId);
	}

	/**
	 * 移除所有观众<br>
	 * 
	 * @param streamId
	 */
	public void removeAudienceAll(String streamId) {
		template.delete(ROOM_AUDIENCE_KEY + streamId);
	}

	/**
	 * 添加请求连接<br>
	 * 
	 * @param roomUser
	 */
	public void addRequestConnect(RoomUser roomUser) {
		HashOperations<String, Object, Object> hashRedis = template.opsForHash();
		hashRedis.put(ROOM_REQUEST_CONNECT_KEY + roomUser.getStreamId(), roomUser.getUuid(),
				JSONObject.toJSONString(roomUser));
	}

	/**
	 * 获取请求<br>
	 * 
	 * @param streamId
	 * @param userId
	 * @return
	 */
	public RoomUser getRequestConnect(String streamId, String userId) {
		HashOperations<String, Object, Object> hashRedis = template.opsForHash();
		Object obj = hashRedis.get(ROOM_REQUEST_CONNECT_KEY + streamId, userId);
		if (obj == null) {
			return null;
		}
		return JSONObject.parseObject(obj.toString(), RoomUser.class);
	}

	/**
	 * 获取所有请求<br>
	 * 
	 * @param streamId
	 * @return List<RoomUser>
	 */
	public List<RoomUser> getRequestConnectAll(String streamId) {
		HashOperations<String, Object, Object> hashRedis = template.opsForHash();
		Map<Object, Object> users = hashRedis.entries(ROOM_REQUEST_CONNECT_KEY + streamId);
		List<RoomUser> userList = new ArrayList<RoomUser>();
		if (users == null) {
			return userList;
		}
		Iterator<Entry<Object, Object>> iter = users.entrySet().iterator();
		while (iter.hasNext()) {
			userList.add(JSONObject.parseObject(iter.next().getValue().toString(), RoomUser.class));
		}
		Collections.sort(userList, (roomUser1, roomUser2) -> {
			Date time1 = roomUser1.getCreateTime() == null ? new Date() : roomUser1.getCreateTime();
			Date time2 = roomUser2.getCreateTime() == null ? new Date() : roomUser2.getCreateTime();
			return time2.compareTo(time1);
		});
		return userList;
	}

	/**
	 * 移除请求连接<br>
	 * 
	 * @param streamId
	 * @param userId
	 */
	public void removeRequestConnect(String streamId, String userId) {
		HashOperations<String, Object, Object> hashRedis = template.opsForHash();
		hashRedis.delete(ROOM_REQUEST_CONNECT_KEY + streamId, userId);
	}

	/**
	 * 移除请求连接<br>
	 * 
	 * @param streamId
	 * @param userId
	 */
	public void removeRequestConnectAll(String streamId) {
		template.delete(ROOM_REQUEST_CONNECT_KEY + streamId);
	}

	/**
	 * 放入队列中<br>
	 * 
	 * @param streamId
	 */
	public void pushRoom(String streamId) {
		logger.info("push room, size = " + template.opsForList().size(ROOM_PUSH_KEY));
		template.opsForList().leftPush(ROOM_PUSH_KEY, streamId);
	}

	/**
	 * 获取队列中值<br>
	 * 
	 * @param value
	 */
	public String PopRoom() {
		return template.opsForList().rightPop(ROOM_PUSH_KEY);
	}

	/**
	 * 获取队列中值<br>
	 * 
	 * @param value
	 */
	public String PopCreateRoom() {
		return template.opsForList().rightPop(CREATE_ROOM_PUSH_KEY);
	}

	/**
	 * 放入队列中<br>
	 * 
	 * @param streamId
	 */
	public void pushCreateRoom(String streamId) {
		template.opsForList().leftPush(CREATE_ROOM_PUSH_KEY, streamId);
	}

	/**
	 * 创建房间信息<br>
	 * 
	 * @param stream
	 *            视频信息
	 * @param groupId
	 *            分组id
	 */
	public Room createRoom(Stream stream, String groupId, Integer onLine) {
		// step:1 创建房间信息
		Room room = new Room();
		room.setPublishUri(stream.getPublishUri());
		room.setPlayUri(stream.getPlayUri());
		room.setUuid(stream.getUuid());
		room.setCreateTime(new Date());
		room.setParentId(stream.getParentId());
		room.setUpdateTime(new Date());
		room.setPraiseCount(stream.getPraiseCount());
		room.setOnLineCount(onLine);
		room.setChildRoom(new ArrayList<Room>());
		room.setChildRoomUser(new ArrayList<RoomUser>());
		room.setNotAllowUsers(new ArrayList<String>());
		room.setBanRoomUserList(new ArrayList<String>());
		room.setStatus(stream.getStatus());
		room.setGroupId(groupId);
		room.setBannerId(stream.getBannerId());
		room.setName(stream.getName());

		room.setRoomUser(getRoomUser(stream.getUserId()));
		return room;
	}

	/**
	 * 主播关闭直播
	 * 
	 * @param parentId
	 * @param userId
	 * @return
	 */
	public JsonData anchorClose(String parentId, String userId) {// 主播断开直播，是取出所有的子连接，并移除连接、请求、观众列表
		List<String> closeList = new ArrayList<String>();// 关闭streamIds
		Stream stream = streamService.selectStream(parentId);
		if (stream == null) {// 视频不存在
			logger.info("closeConnect->关闭直播不存在：" + parentId);
			return JsonData.success();
		}

		Room room = getRoom(parentId);// 获取房间信息
		if (room == null) {
			logger.info("closeConnect->关闭room不存在：" + parentId);
			return JsonData.success();
		}
		Iterator<Stream> childRooms = streamService.findConnectings(null, parentId).iterator();
		while (childRooms.hasNext()) {
			closeList.add(childRooms.next().getParentId());
		}
		if (stream.getStatus() == 0) {// 创建状态
			removeAudienceAll(parentId);// 移除所有观众
			streamService.deleteStream(stream.getUserId(), stream.getUuid());
			removeRoom(parentId);// 移除直播房间
		} else {
			closeList.add(parentId);
			// roomService.removeAudienceAll(parentId);//移除所有观众
			room.setStatus(2);// 更改room状态
			createRoom(room);
		}
		removeChildRoom(parentId);// 移除所有连接
		removeRequestConnectAll(parentId);// 移除所有请求
		RoomListeners.deleteListener(parentId);// 移除监听放到监控中
		if (room != null) {
			if (stream.getStatus() == 1) {// 直播记录时长
				// 记录直播时长
				long watchTime = (long) ((new Date().getTime() - room.getCreateTime().getTime()) / 1000);
				watchStreamService.save(userId, parentId, watchTime, WatchType.Stream.name());
			}
			Map<String, Object> extInfo = new HashMap<String, Object>();// 扩展信息
			extInfo.put("groupId", room.getGroupId());
			extInfo.put("playbackUri", stream.getPlaybackUri());
			extInfo.put("messageSource", Message.Source.StreamEnded.name());
//			pushService.push(userId, room.getGroupId(), JSONObject.toJSONString(extInfo));
			messageService.sendGroupMessage(room.getGroupId(), "", extInfo, null);
			// 发给主播
			extInfo.clear();
			extInfo.put("messageSource", Message.Source.StreamSummary.name());

			// TODO 是否主播需要处理内容
			String desc = configureUtil.getConfig("streamSummaryDescription");
			if (StringUtils.isEmpty(desc))
				desc = "您的直播已结束，直播开始于：start，结束于：end，时长：duration，观看人次：playCount，投标数：bidNum，任务数：bonusTaskNum，去个人中心继续处理>>";
			desc = desc.replaceAll("start", DateUtil.dateToString(stream.getStarted(), DateUtil.FORMAT_STANDERD_CN))
					.replaceAll("end", DateUtil.dateToString(new Date(), DateUtil.FORMAT_STANDERD_CN))
					.replaceAll("duration",
							DateUtil.duration(stream.getStarted(),
									stream.getEnded() == null ? new Date() : stream.getEnded()))
					.replaceAll("playCount", String.valueOf(stream.getPlayCount()))
					.replaceAll("bidNum",
							String.valueOf(interactionService.getNumber(stream.getUuid(), InteractionType.Bid)))
					.replaceAll("bonusTaskNum",
							String.valueOf(interactionService.getNumber(stream.getUuid(), InteractionType.BonusTask)));

			List<String> userIds = new ArrayList<>();
			userIds.add(userId);
//			pushService.message(userIds, desc, JSONObject.toJSONString(extInfo));
			messageService.sendSystemMessage(desc, extInfo, userId);

		}
		// 更新断开连接stream状态（结束）
		if (closeList.size() < 1) {
			logger.info("需要关闭的stream为空");
			return JsonData.fail(ErrorCode.NO_LIVE_NEED_CLOSE);
		}
		logger.info("关闭streamIds->" + closeList);
		Integer status = streamService.closeStreams(closeList);// 关闭连接
		if (status.intValue() < 1) {
			logger.info("关闭视频直播失败：" + JSONObject.toJSONString(closeList));
		}
		PushStreamTerminal t = terminalService.getByStreamId(parentId);
		if(t!=null){
			srsService.pauseTerminalPublish(t);
		}
		// 推送room信息
		pushRoom(parentId);
		return JsonData.success();
	}
	
	/**
	 * 根据事件获取相关stream
	 * @param callbackStreamName
	 * @return
	 */
	public Stream getRelatedStream(String callbackStreamName){
		if(StringUtils.isEmpty(callbackStreamName)) return null;
		Stream s = streamService.findStreamById(callbackStreamName);
		if(s==null){
			String publishUrl = configureUtil.getConfig(Constants.PUBLISH_DOMAIN) + "/" + configureUtil.getConfig(Constants.APP_NAME) + "/" + callbackStreamName;
			s = terminalService.getLatestStream(publishUrl);
		}
		return s;
	}
	
	public RoomUser getRoomUser(String userId) {

		UserVo userVo = userService.getUserVoByUserId(userId);

		RoomUser roomUser = new RoomUser();
		String avatar = "";
		String nickname = "";
		int isVip = 0;

		UserProfileVo profile = userVo.getProfile();

		// JSONObject profile = userObj.getJSONObject("profile");
		if (profile != null) {
			logger.info("userObj profile, " + JSONObject.toJSONString(profile));
			avatar = profile.getAvatar();
			nickname = profile.getNickname();
		}

		List<HashMap<String, String>> roles = userVo.getRoles();

		if (roles != null && !roles.isEmpty()) {
			for (Map<String, String> role : roles) {
				if (role.containsValue(Role.RoleType.V.getCode())) {
					isVip = 1;
				}
			}
		} else {
			logger.info("userObj roles is null");
		}
		roomUser.setRoles(roles);
		roomUser.setIsVip(isVip);
		roomUser.setUuid(userId);
		roomUser.setNickName(nickname);
		roomUser.setAvatar(avatar);
		roomUser.setCreateTime(new Date());
		roomUser.setProfile(profile);

		return roomUser;
	}

	private Map<String, Object> roomUserToMap(RoomUser roomUser) {
		Map<String, Object> userMap = new HashMap<String, Object>();
		userMap.put("uuid", roomUser.getUuid());
		userMap.put("isFollow", roomUser.getIsFollow());// 是否关注主播
		userMap.put("isVip", roomUser.getIsVip());// 是否关注主播
		userMap.put("profile", roomUser.getProfile());
		userMap.put("roles", roomUser.getRoles());
		return userMap;
	}
}
