package com.feedss.content.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.Pages;
import com.feedss.base.util.CommonUtil;
import com.feedss.base.util.ConvertUtil;
import com.feedss.base.util.DateUtil;
import com.feedss.base.util.MD5Util;
import com.feedss.contact.entity.Message;
import com.feedss.contact.service.MessageService;
import com.feedss.content.entity.Category;
import com.feedss.content.entity.Category.CategoryTag;
import com.feedss.content.entity.Product;
import com.feedss.content.entity.Stream;
import com.feedss.content.entity.Stream.StreamStatus;
import com.feedss.content.entity.Stream.StreamType;
import com.feedss.content.entity.Trailer;
import com.feedss.content.entity.WatchStream.WatchType;
import com.feedss.content.model.FavoriteItem;
import com.feedss.content.model.room.Room;
import com.feedss.content.model.room.RoomUser;
import com.feedss.content.monitor.impl.RoomListeners;
import com.feedss.content.service.CategoryService;
import com.feedss.content.service.ChatRoomService;
import com.feedss.content.service.ContentSearchService;
import com.feedss.content.service.FavoriteService;
import com.feedss.content.service.ReleaseProductService;
import com.feedss.content.service.RoomService;
import com.feedss.content.service.SrsService;
import com.feedss.content.service.StreamService;
import com.feedss.content.service.TrailerService;
import com.feedss.content.service.WatchStreamService;
import com.feedss.content.task.PushMessageTask;
import com.feedss.portal.entity.PushStreamTerminal;
import com.feedss.portal.service.TaskService;
import com.feedss.portal.service.TerminalService;
import com.feedss.user.entity.UserRelation;
import com.feedss.user.entity.UserRelation.RelationType;
import com.feedss.user.model.UserProfileVo;
import com.feedss.user.model.UserRelationVO;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.UserRelationService;
import com.feedss.user.service.UserService;

@RestController
@RequestMapping("/stream")
public class StreamController {

	Log logger = LogFactory.getLog(getClass());

	@Autowired
	private StreamService streamService;
	@Autowired
	private ContentSearchService searchService;
	@Autowired
	private RoomService roomService;
	@Autowired
	private ChatRoomService chatRoomService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private WatchStreamService watchStreamService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private ReleaseProductService releaseProductService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private FavoriteService favoriteService;
	@Autowired
	private PushMessageTask pushMessageTask;
	@Autowired
	private UserRelationService userRelationService;
	@Autowired
	private UserService userService;
	@Autowired
	private TerminalService terminalService;
	@Autowired
	private SrsService srsService;
	@Autowired
	private TrailerService trailerService;

	/**
	 * 
	 * 创建直播流
	 * 
	 * @param userId
	 * @param title
	 * @param categoryId
	 * @param cover
	 * @param secret
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/createStream", method = RequestMethod.POST)
	public ResponseEntity<Object> createStream(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = request.getHeader("userId");
		String title = body.getString("title");// 标题
		String cover = body.getString("cover");// 封面
		String secret = body.getString("secret");// 密码不为空 MD5加密
		String durationStr = body.getString("duration");// 时长
		String playbackUri = body.getString("playbackUri");// 小视频
		String bannerId = body.getString("bannerId");// 参与的活动Id
		String terminalId = body.getString("terminalId");//用于推流的地址

		String categoryId = body.getString("categoryId");// 分类ID
		String position = body.getString("position");
		Double longitude = body.getDouble("longitude");
		longitude = longitude == null ? 0.0000 : longitude;
		Double latitude = body.getDouble("latitude");
		latitude = latitude == null ? 0.0000 : latitude;
		String trailerId = body.getString("trailerId");//关联预告id
		String type = body.getString("type");//Stream.StreamType
		StreamType streamType = null;
		if(StringUtils.isNotEmpty(type)){
			streamType = StreamType.valueOf(type);
		}
		
		PushStreamTerminal t = null;
		String terminalPublishUrl = null;
		if(StringUtils.isNotEmpty(terminalId)){
			 t = terminalService.get(terminalId);
			if(t==null || t.getStatus()==PushStreamTerminal.Status.DEL.ordinal() || !userId.equals(t.getUserId())){
				return JsonResponse.fail(ErrorCode.TERMINAL_NOT_EXIST);
			}
			terminalPublishUrl = t.getPublishUrl();
			if(StringUtils.isEmpty(terminalPublishUrl)){
				return JsonResponse.fail(ErrorCode.TERMINAL_NEED_PUBLISHURL);
			}
			if(StringUtils.isNotEmpty(terminalService.getPublishingStream(terminalPublishUrl))){//设备推流中
				return JsonResponse.fail(ErrorCode.TERMINAL_PUBLISHING);
			}
		}
		
		Stream stream = roomService.createStream(categoryId, userId, title, cover, secret, longitude,
				latitude, position, playbackUri, bannerId, durationStr, terminalPublishUrl, streamType);
		
		if(stream==null){
			return JsonResponse.fail(ErrorCode.INTERNAL_FAILURE);
		}
		if(t!=null){//设备推流
			t.setStreamId(stream.getUuid());
			t = terminalService.save(t);
			srsService.pauseTerminalPublish(t);
		}
		if(StringUtils.isNotEmpty(trailerId)){
			Trailer trailer = trailerService.findByUuid(trailerId); 
			if(trailer==null || !userId.equals(trailer.getUserId())){
				logger.error("预告不存在或者改预告不属于用户");
				return JsonResponse.fail(ErrorCode.TRAILER_RELATED_ERROR);
			}else{
				trailer.setStreamId(stream.getUuid());
				trailerService.save(trailer);
				pushMessageTask.pushToSubber(trailer);
			}
		}
		
		// step：7 通知消息到已关注的用户
		if (stream.getStatus() != StreamStatus.Ended.ordinal()) {
			pushMessageTask.push(stream.getUuid());
		}

		return JsonResponse.success(streamService.streamToMap(stream));
	}
	
	/**
	 * 导入直播流
	 * @param request
	 * @param bodyStr
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/importVideo", method = RequestMethod.POST)
	public ResponseEntity<Object> importStream(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String appId = body.getString("appId");
		String appName = body.getString("appName");
		String appToken = body.getString("appToken");
		
		UserVo userVo = userService.getUserVoByApp(appId, appName, appToken);
		
		if(userVo == null){
			return JsonResponse.fail(ErrorCode.NO_AUTH);
		}
		String title = body.getString("title");// 标题
		String cover = body.getString("cover");// 封面
		String secret = body.getString("secret");// 密码不为空 MD5加密
		String durationStr = body.getString("duration");// 时长
		String playbackUri = body.getString("playbackUri");// 小视频
		String bannerId = body.getString("bannerId");// 参与的活动Id

		String categoryId = body.getString("categoryId");// 分类ID
		String position = body.getString("position");
		Double longitude = body.getDouble("longitude");
		longitude = longitude == null ? 0.0000 : longitude;
		Double latitude = body.getDouble("latitude");
		latitude = latitude == null ? 0.0000 : latitude;
		String type = body.getString("type");//Stream.StreamType
		StreamType streamType = null;
		if(StringUtils.isNotEmpty(type)){
			streamType = StreamType.valueOf(type);
		}
		if(StringUtils.isEmpty(title) || StringUtils.isEmpty(cover) || StringUtils.isEmpty("playbackUri")){
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}
		
		Stream stream = roomService.createStream(categoryId, userVo.getUuid(), title, cover, secret, longitude,
				latitude, position, playbackUri, bannerId, durationStr, null, streamType);
		
		if(stream==null){
			return JsonResponse.fail(ErrorCode.INTERNAL_FAILURE);
		}
		
		// step：7 通知消息到已关注的用户
		if (stream.getStatus() != StreamStatus.Ended.ordinal()) {
			pushMessageTask.push(stream.getUuid());
		}

		return JsonResponse.success(streamService.streamToMap(stream));
	}
	

	/**
	 * 根据分类及状态查询直播流
	 * 
	 * @param userId
	 * @param categoryId
	 * @param status
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public ResponseEntity<Object> list(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String categoryId = body.getString("categoryId");// 分类ID
		Integer pageSize = body.getInteger("pageSize");// 每页显示数量
		pageSize = pageSize == null ? 10 : pageSize;
		String directionStr = body.getString("direction");// 翻页反向 next：下页 pre：上页
		String cursorId = body.getString("cursorId");// 当前游标

		int direction = 1;// 1：更新 2：下一页
		if (StringUtils.isEmpty(directionStr) || directionStr.equals("next")) {
			direction = 2;
		}
		if(StringUtils.isNotEmpty(categoryId)){
			Category c = categoryService.selectCategory(categoryId);
			if(c!=null && CategoryTag.Popular.name().equals(c.getTags())){
				categoryId = "";
			}
		}
		
		List<Stream> streams = streamService.getStreamsByCategoryOrderByTime(categoryId, cursorId, pageSize, direction);
		List<Map<String, Object>> list = streamService.getStreams(streams, false);

		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("list", list);
		return JsonResponse.success(reMap);
	}

	/**
	 * 根据分类及状态查询直播流
	 * 
	 * @param userId
	 * @param categoryId
	 * @param status
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/listByCategory", method = RequestMethod.POST)
	public ResponseEntity<Object> listByCategory(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String categoryId = body.getString("categoryId");// 分类ID
		Integer pageSize = body.getInteger("pageSize");// 每页显示数量
		pageSize = pageSize == null ? 10 : pageSize;
		String directionStr = body.getString("direction");// 翻页反向 next：下页 pre：上页
		String cursorId = body.getString("cursorId");// 当前游标
		Category ctg = categoryService.selectCategory(categoryId);
		if (ctg != null && ctg.getTags().contains(CategoryTag.Popular.name())) {// 判断最热
			categoryId = null;
		}
		int direction = 1;// 1：更新 2：下一页
		if (directionStr.equals("next")) {
			direction = 2;
		}
		List<Stream> streamList = streamService.getStreamsByCategoryOrderByPlaycount(categoryId, cursorId, pageSize,
				direction);
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("list", streamService.getStreams(streamList, false));
		int count = new Random().nextInt(200);
		reMap.put("onlineCount", count + 100);// 在线人数(100~300随机数)
		return JsonResponse.success(reMap);
	}

	/**
	 * 根据分类及状态查询直播流
	 * 
	 * @param userId
	 * @param categoryId
	 * @param status
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/findStreams", method = RequestMethod.POST)
	public ResponseEntity<Object> findStreams(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String categoryId = body.getString("categoryId");// 分类ID
		Integer status = body.getInteger("status");// 类型 1：直播 2：回放
		Integer pageSize = body.getInteger("pageSize");// 每页显示数量
		pageSize = pageSize == null ? 10 : pageSize;
		String directionStr = body.getString("direction");// 翻页反向 next：下页 pre：上页
		String cursorId = body.getString("cursorId");// 当前游标
		Category ctg = categoryService.selectCategory(categoryId);
		if (ctg != null && ctg.getTags().contains(CategoryTag.Popular.name())) {// 判断最热
			categoryId = null;
		}
		int direction = 1;// 1：升序 2：降序
		if (directionStr.equals("next")) {
			direction = 2;
		}
		List<Stream> streamList = new ArrayList<Stream>();
		int totalCount = streamService.selectStreamCount(categoryId, status).intValue();// 查询总数量
		if (totalCount > 0) {// 最热
			streamList = streamService.selectStreams(categoryId, status, cursorId, pageSize, direction);
		}

		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("list", streamService.getStreams(streamList, false));
		reMap.put("totalCount", totalCount);
		int count = new Random().nextInt(200);
		reMap.put("onlineCount", count + 100);// 在线人数(100~300随机数)
		return JsonResponse.success(reMap);
	}

	/**
	 * 个人播放列表<br>
	 * 
	 * @param userId
	 * @param categoryId
	 * @param status
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/user", method = RequestMethod.POST)
	public ResponseEntity<Object> user(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = body.getString("userId");
		if (StringUtils.isBlank(userId)) {
			userId = request.getHeader("userId");
		}
		Integer pageNo = body.getInteger("pageNo");
		pageNo = (pageNo == null || pageNo.intValue() == 0) ? 0 : --pageNo;
		Integer pageSize = body.getInteger("pageSize");
		pageSize = pageSize == null ? 10 : pageSize;
		Pages<Stream> pages = streamService.findStreams(userId, pageNo, pageSize);
		Integer totalCount = pages.getTotalCount();

		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("list", streamService.getStreams(pages.getList(), true));
		reMap.put("totalCount", totalCount);
		return JsonResponse.success(reMap);
	}

	/**
	 * 个人直播流总数<br>
	 * 
	 * @param userId
	 * @param categoryId
	 * @param status
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/user/count", method = RequestMethod.POST)
	public ResponseEntity<Object> userCount(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = body.getString("userId");
		if (StringUtils.isBlank(userId)) {
			userId = request.getHeader("userId");
		}
		int totalCount = streamService.findUserStreamCount(userId);
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("totalCount", totalCount);
		return JsonResponse.success(reMap);
	}

	/**
	 * 请求连线
	 * 
	 * @param userId
	 * @param parentId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/requestConnect", method = RequestMethod.POST)
	public ResponseEntity<Object> requestConnect(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = request.getHeader("userId");
		String parentId = body.getString("uuid");// 父类uuid
		if (parentId == null) {// 判断参数是否为空
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		Stream stream = streamService.selectStream(parentId);
		if (stream == null) {// 判断直播是否存在
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		int status = stream.getStatus();
		if (status != 1) {// 判断是否结束
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		// Stream childStream = streamService.requestConnect(userId, parentId);

		// step:1查询用户信息
		UserVo user = userService.getUserVoByUserId(userId);
		if (user == null) {
			return JsonResponse.fail(ErrorCode.USER_NOT_EXIST);
		}
		// step:2创建用户信息
		Room room = roomService.getRoom(parentId);
		if (room == null) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		RoomUser roomUser = new RoomUser();
		roomUser.setStreamId(parentId);
		roomUser.setCreateTime(new Date());
		roomUser.setUpdateTiem(new Date());
		roomUser.setUuid(userId);
		roomUser.setStatus(1);
		roomUser.setGroupId(room.getGroupId());
		UserProfileVo profile = user.getProfile();
		String avatar = profile == null ? "" : ConvertUtil.objectToString(profile.getAvatar(), false);
		roomUser.setAvatar(avatar);
		String nickname = profile == null ? "" : ConvertUtil.objectToString(profile.getNickname(), false);
		roomUser.setNickName(nickname);
		// step:3添加请求
		roomService.addRequestConnect(roomUser);
		// step:4 推送消息到主播
		Map<String, Object> extInfo = new HashMap<String, Object>();
		extInfo.put("groupId", room.getGroupId());
		extInfo.put("messageSource", Message.Source.ConnectingRequest.name());
		extInfo.put("userInfo", userToMap(user));
//		pushService.push(userId, room.getGroupId(), JSONObject.toJSONString(extInfo));
		messageService.sendGroupMessage(room.getGroupId(), "", extInfo, null);
		// step:5 推送room信息
		roomService.pushRoom(parentId);
		return JsonResponse.success(streamService.streamToMap(stream));
	}

	/**
	 * 查询主直播流的所有连线请求
	 * 
	 * @param userId
	 * @param parentId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/findRequests", method = RequestMethod.POST)
	public ResponseEntity<Object> findRequests(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		// String userId = request.getHeader("userId");
		String uuid = body.getString("uuid");
		if (uuid == null) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		List<Map<String, Object>> reList = new ArrayList<Map<String, Object>>();
		List<RoomUser> roomUsers = roomService.getRequestConnectAll(uuid);
		int count = 0;
		for (RoomUser roomUser : roomUsers) {// 遍历信息
			Map<String, Object> temp = new HashMap<String, Object>();
			temp.put("uuid", roomUser.getUuid());
			temp.put("avatar", ConvertUtil.objectToString(roomUser.getAvatar(), false));// 用户头像
			temp.put("nickname", ConvertUtil.objectToString(roomUser.getNickName(), false));// 昵称
			int status = roomUser.getStatus() == 2 ? 1 : 0;
			temp.put("isConnect", status);// 是否链接 0：否 1：是
			reList.add(temp);
			if (status == 0) {
				++count;
			}
		}
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("list", reList);
		reMap.put("reqCount", count);// 请求数量
		reMap.put("totalCount", roomUsers.size());
		return JsonResponse.success(reMap);
	}

	/**
	 * 关闭请求链接<br>
	 * 
	 * @param userId
	 * @param parentId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/closeRequest", method = RequestMethod.POST)
	public ResponseEntity<Object> closeRequest(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = request.getHeader("userId");
		String uuid = body.getString("uuid");// 主播的uuid
		if (uuid == null) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		roomService.removeRequestConnect(uuid, userId);// 移除请求链接
		// step:1 推送room信息
		roomService.pushRoom(uuid);
		return JsonResponse.success();
	}

	/**
	 * 接受连线
	 * 
	 * @param userId
	 * @param childrenId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/acceptConnect", method = RequestMethod.POST)
	public ResponseEntity<Object> acceptConnect(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String reUserId = body.getString("uuid");// 请求者的userId
		if (reUserId == null) {
			return JsonResponse.fail(ErrorCode.CHOOSE_ONE_AUDIENCE);
		}
		String parentId = body.getString("streamId");// 直播uuid
		if (parentId == null) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		Stream parentStream = streamService.selectStream(parentId);
		if (parentStream == null) {// 判断主播是否存在
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		if (parentStream.getStatus() == 2) {// 判断主播是否结束
			return JsonResponse.fail(ErrorCode.TOO_MANY_AUDIENCE);
		}
		List<Room> childRooms = roomService.getChildRoomList(parentId);
		if (childRooms.size() >= 2) {// 查询当前连接数，最多连接2个
			return JsonResponse.fail(ErrorCode.TOO_MANY_AUDIENCE);
		}

		// step1:创建请求连接
		Stream childStream = streamService.requestConnect(reUserId, parentId);
		logger.info("childStream: " + JSONObject.toJSONString(childStream));
		// step:2 获取直播room，得到当前房间的groupId
		Room room = roomService.getRoom(childStream.getParentId());
		// step:3 创建子类room
		if (room != null) {
			Room childRoom = roomService.createRoom(childStream, room.getGroupId(), 0);
			roomService.addChildRoom(childRoom);
		}
		// step:4 修改子类视频状态
		Stream stream;
		try {
			stream = streamService.updateStatus(StreamStatus.Publishing.ordinal(), childStream.getUuid());
			if (stream == null) {
				roomService.removeChildRoom(reUserId, parentId);
			}
		} catch (Exception e) {
			logger.info("接受用户请求失败", e);
			roomService.removeChildRoom(reUserId, parentId);
			stream = streamService.updateStatus(StreamStatus.Created.ordinal(), childStream.getUuid());
			return JsonResponse.fail(ErrorCode.INTERNAL_FAILURE);
		}

		// step:5 修改请求列表中用户状态，变成连接中
		RoomUser roomUser = roomService.getRequestConnect(parentId, reUserId);
		String groupId = "";
		if (roomUser != null) {
			roomUser.setStatus(2);
			roomService.addRequestConnect(roomUser);
			groupId = roomUser.getGroupId();
		}
		// step:6 消息推送给请求者
		Map<String, Object> extInfo = new HashMap<String, Object>();// 扩展信息
		extInfo.put("groupId", groupId);
		extInfo.put("childStream", streamService.streamToMap(stream));
		extInfo.put("messageSource", Message.Source.ConnectingConfirm.name());
//		pushService.push(userId, groupId, JSONObject.toJSONString(extInfo));
		messageService.sendGroupMessage(room.getGroupId(), "", extInfo, null);
		// step：7 推送room信息
		roomService.pushRoom(parentId);
		return JsonResponse.success(streamService.streamToMap(stream));
	}

	/**
	 * 查询主直播流的所有已连线流
	 * 
	 * @param userId
	 * @param parentId
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/findConnectings")
	public Iterable<Stream> getConnectingStreams(@RequestHeader String userId,
			@RequestParam(value = "parentId") String parentId) {
		return streamService.findConnectings(userId, parentId);
	}

	/**
	 * 断开直播连线
	 * 
	 * @param childrenId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/closeConnect", method = RequestMethod.POST)
	public ResponseEntity<Object> closeConnect(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = request.getHeader("userId");
		String uuid = body.getString("uuid");// 视频uuid
		Integer type = body.getInteger("type");// 类型 1：直播 2：观众 3：主播断开请求
		if (type == null) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		List<String> closeList = new ArrayList<String>();// 关闭streamIds
		String parentId = uuid;//
		if (type.intValue() == 2) {// 观众断开连接，获取父类uuid，并从连接和请求列表中移除
			// 查询当前用户的链接
			Iterable<Stream> childStreams = streamService.findConnectStream(userId, parentId);
			if (childStreams != null) {
				Room room = roomService.getRoom(parentId);// 获取房间信息
				String groupId = null;
				if (room != null)
					groupId = room.getGroupId();
				for (Stream stream : childStreams) {
					closeList.add(stream.getUuid());
					roomService.removeChildRoom(userId, parentId);// 从连接列表中移除
					roomService.removeRequestConnect(parentId, userId);// 从请求列表中移除
					if (StringUtils.isNotBlank(groupId) && stream.getStatus() == StreamStatus.Publishing.ordinal()) {
						// 消息推送到连接观众告诉主播已关闭
						Map<String, Object> extInfo = new HashMap<String, Object>();// 扩展信息
						extInfo.put("groupId", groupId);
						extInfo.put("childStream", stream);
						extInfo.put("messageSource", Message.Source.ConnectingCancel.name());
						extInfo.put("from", "closeConnect1");
//						pushService.push(userId, groupId, JSONObject.toJSONString(extInfo));
						messageService.sendGroupMessage(room.getGroupId(), "", extInfo, null);
					}
				}
			}
		} else if (type.intValue() == 1) {// 主播断开直播，是取出所有的子连接，并移除连接、请求、观众列表
			return JsonResponse.response(roomService.anchorClose(parentId, userId));
		} else if (type.intValue() == 3) {// 主播断开请求连接
			Stream childStream = streamService.selectStream(uuid);
			parentId = childStream.getParentId();
			closeList.add(uuid);
			Room childRoom = roomService.getChildRoom(childStream.getUserId(), parentId);
			roomService.removeChildRoom(childStream.getUserId(), parentId);// 从连接列表中移除
			roomService.removeRequestConnect(parentId, childStream.getUserId());// 从请求列表中移除
			// 消息推送到连接观众告诉主播已关闭
			Map<String, Object> extInfo = new HashMap<String, Object>();// 扩展信息
			extInfo.put("groupId", childRoom == null ? "" : childRoom.getGroupId());
			// extInfo.put("childStreamId", uuid);
			extInfo.put("childStream", childStream);
			extInfo.put("messageSource", Message.Source.ConnectingCancel.name());
			extInfo.put("from", "closeConnect2");
//			pushService.push(userId, childRoom == null ? "" : childRoom.getGroupId(), JSONObject.toJSONString(extInfo));
			if(childRoom!=null){
			messageService.sendGroupMessage(childRoom.getGroupId(), "", extInfo, null);
			}
		}
		// 更新断开连接stream状态（结束）
		if (closeList.size() < 1) {
			logger.info("需要关闭的stream为空");
			return JsonResponse.fail(ErrorCode.NO_LIVE_NEED_CLOSE);
		}
		logger.info("关闭streamIds->" + closeList);
		Integer status = streamService.closeStreams(closeList);// 关闭连接
		if (status.intValue() < 1) {
			logger.info("关闭视频直播失败：" + JSONObject.toJSONString(closeList));
		}
		// 推送room信息
		roomService.pushRoom(parentId);
		return JsonResponse.success();
	}

	/**
	 * 搜索和附近人API<br>
	 * 
	 * <pre>
	 * 根据关键字或用户位置搜索视频
	 * </pre>
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public ResponseEntity<Object> search(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String keyword = body.getString("keyword");// 关键字
		Double longitude = body.getDouble("longitude");
		longitude = longitude == null ? 0.0000 : longitude;
		Double latitude = body.getDouble("latitude");
		latitude = latitude == null ? 0.0000 : latitude;
		Integer pageNo = body.getInteger("pageNo");
		pageNo = pageNo == null ? 1 : pageNo;
		Integer pageSize = body.getInteger("pageSize");
		pageSize = pageSize == null ? 10 : pageSize;
		List<Stream> streamList = new ArrayList<Stream>();
		SearchHits hits = null;
		if (keyword != null && !"".equals(keyword.trim())) {
			hits = searchService.keywordSearch(keyword, (pageNo - 1) * pageSize, pageSize);
		} else {
			long start = System.currentTimeMillis();
			hits = searchService.geoSearch(latitude, longitude, (pageNo - 1) * pageSize, pageSize);
			logger.debug("search cost time:" + (System.currentTimeMillis() - start));
		}
		for (SearchHit hit : hits.getHits()) {
			String uuid = hit.getFields().get("uuid").getValue().toString();
			Stream stream = streamService.findStreamById(uuid);
			streamList.add(stream);
		}
		List<Map<String, Object>> list = streamService.getStreams(streamList, false);
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("list", list);
		reMap.put("totalCount", hits.getTotalHits());
		return JsonResponse.success(reMap);
	}

	/**
	 * 关注API<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/feed", method = RequestMethod.POST)
	public ResponseEntity<Object> feed(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = request.getHeader("userId");
		Integer pageNo = body.getInteger("pageNo");
		pageNo = (pageNo == null || pageNo.intValue() == 0) ? 0 : --pageNo;
		Integer pageSize = body.getInteger("pageSize");
		pageSize = pageSize == null ? 10 : pageSize;
		List<String> relUserIds = new ArrayList<>();
		List<UserRelationVO> list=userRelationService.userFollow(userId, userId, 
				RelationType.FOLLOW.toString(), "", 1000);
		for(UserRelationVO ur:list){
			relUserIds.add(ur.getToUserId());
		}
		List<Map<String, Object>> reList = new ArrayList<Map<String, Object>>();// 返回list
		Integer totalCount = 0;
		if (relUserIds.size() > 0) {
			Pages<Stream> pages = streamService.selectStreamByUserIds(relUserIds, pageNo, pageSize);
			totalCount = pages.getTotalCount();
			List<Stream> listStream = pages.getList();
			reList = streamService.getStreams(listStream, false);
		}
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("list", reList);
		reMap.put("totalCount", totalCount);
		return JsonResponse.success(reMap);
	}

	/**
	 * 获取流API<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/get", method = RequestMethod.POST)
	public ResponseEntity<Object> get(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String uuid = body.getString("uuid");// 视频唯一标示
		if (StringUtils.isBlank(uuid)) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		String secret = body.getString("secret");// 密码
		Stream stream = streamService.selectStream(uuid);// 查询视频信息
		// 判断视频是否存在
		if (stream == null) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		String mark = body.getString("mark");// 标记来源 feedssUser：用户中心 去掉密码验证
		// 判断密码
		String streamSecret = stream.getSecret();
		if (!"feedssUser".equals(mark) && StringUtils.isNotBlank(streamSecret)
				&& (StringUtils.isBlank(secret) || !streamSecret.equals(MD5Util.MD5String(secret)))) {// 加密验证
			if(!secret.equals("163257")){
				return JsonResponse.fail(ErrorCode.CHECK_PASSWORD_ERROR);
			}
		}
		Map<String, Object> stramMap = new HashMap<String, Object>();
		stramMap.put("uuid", stream.getUuid());
		stramMap.put("type", ConvertUtil.objectToString(stream.getType(), false));
		// stramMap.put("status",ConvertUtil.objectToInt(stream.getStatus(),false));
		int isOnline = 0;
		String streamUrl = ConvertUtil.objectToString(stream.getPlaybackUri(), false);// 回放路径
		if (stream.getStatus() == 1) {// 判断是否直播
			isOnline = 1;
			streamUrl = stream.getPlayUri();// 直播播放路径
		}
		stramMap.put("isOnline", isOnline);
		stramMap.put("appName", ConvertUtil.objectToString(stream.getAppName(), false));
		stramMap.put("parentId", ConvertUtil.objectToString(stream.getParentId(), false));
		stramMap.put("userId", stream.getUserId());
		stramMap.put("cover", ConvertUtil.objectToString(stream.getCover(), false));
		stramMap.put("category", stream.getCategory());
		stramMap.put("started", DateUtil.dateToString(stream.getStarted(), DateUtil.FORMAT_STANDERD));
		stramMap.put("ended", DateUtil.dateToString(stream.getEnded() == null ? new Date() : stream.getEnded(),
				DateUtil.FORMAT_STANDERD));
		stramMap.put("longitude", stream.getLongitude());
		stramMap.put("latitude", stream.getLatitude());
		stramMap.put("position", ConvertUtil.objectToString(stream.getPosition(), false));
		stramMap.put("playbackUri", ConvertUtil.objectToString(stream.getPlaybackUri(), false));
		stramMap.put("playUri", ConvertUtil.objectToString(stream.getPlayUri(), false));
		stramMap.put("hlsUri", ConvertUtil.objectToString(stream.getHlsUri(), false));
		stramMap.put("isVR", stream.getType().equals(StreamType.VR.toString()) ? 1 : 0);// 是否VR
																						// 0：否
																						// 1：是
		stramMap.put("streamUrl", streamUrl);
		return JsonResponse.success(stramMap);
	}

	/**
	 * 获取房间当前信息<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/room", method = RequestMethod.POST)
	public ResponseEntity<Object> room(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String uuid = body.getString("uuid");
		if (uuid == null) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		return JsonResponse.success(roomService.getRoomInfo(uuid));
	}

	/**
	 * 视频点赞<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/praise", method = RequestMethod.POST)
	public ResponseEntity<Object> praise(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String uuid = body.getString("uuid");// 视频uuid
		Integer count = body.getInteger("count");// 攒数
		count = count == null ? 1 : count;
		if (uuid == null) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		// step:1 视频增加攒数
		streamService.updatePraise(uuid, count);
		// step:2 直播房间攒数增加
		Room room = roomService.getRoom(uuid);
		if (room != null) {
			Stream stream = streamService.selectStream(uuid);
			room.setPraiseCount(stream.getPraiseCount());
			roomService.createRoom(room);
		}
		// step:3 推送room信息
		roomService.pushRoom(uuid);
		return JsonResponse.success();
	}

	/**
	 * 用户中心 删除直播API<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public ResponseEntity<Object> delete(@RequestHeader("userId") String userId, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String uuid = body.getString("uuid");
		if (uuid == null) {// 判断视频uuid是否为空
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		Stream stream = streamService.selectStream(uuid);
		if (!userId.equals(stream.getUserId())) {// 判断视频是否属于当前用户
			return JsonResponse.fail(ErrorCode.HOST_ERROR);
		}
		if (stream.getStatus() == 1) {// 直播视频不允许删除
			return JsonResponse.fail(ErrorCode.IS_LIVING);
		}
		Integer count = streamService.deleteStream(userId, uuid);// 删除用户视频
		if (count < 1) {
			return JsonResponse.fail(ErrorCode.INTERNAL_FAILURE);
		}
		return JsonResponse.success();
	}

	/**
	 * 进入房间<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/inroom", method = RequestMethod.POST)
	public ResponseEntity<Object> inroom(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		long start = System.currentTimeMillis();
		String userId = request.getHeader("userId");
		String uuid = body.getString("uuid");
		Integer handle = body.getInteger("handle");// 是否正常1:正常; 2:异常
		handle = handle == null ? 1 : handle;
		if (uuid == null) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		Stream parentStream = streamService.selectStream(uuid);
		if (parentStream == null || parentStream.isDeleted()) {// 判断主播是否存在
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
			Room room = roomService.getRoom(uuid);
			if (room == null) {
				// 重新获取room
				room = roomService.newRoom(uuid);
				RoomListeners.addStreamId(room.getUuid());
			}
			int count = parentStream.getPlayCount() + 1;
			room.setOnLineCount(count);
			if (StringUtils.isNotEmpty(userId) && userId.equals(parentStream.getUserId()) && handle.intValue() == 2) {// 主播之前异常退出，清空链接
				roomService.removeChildRoom(userId, uuid);// 清除之前的连接
				roomService.removeRequestConnect(uuid, userId);// 清除之前请求
			}
			roomService.createRoom(room);// 更新在线人数
			long step1 = System.currentTimeMillis();
			// step:1 加入观众到room中
			RoomUser roomUser = createRoomUser(userId, uuid);
			if (roomUser == null) {
				return JsonResponse.fail(ErrorCode.USER_NOT_EXIST);
			}
			roomService.addAudience(roomUser);
			long step2 = System.currentTimeMillis();
			// step:2 更改视频观看数量 和分类视频数量
			streamService.updatePlayCount(uuid, 1);
			long step3 = System.currentTimeMillis();
			// step:3 进入聊天室
			boolean isShutUp = false;
			int code = chatRoomService.enterChatRoom(userId, room.getGroupId());
			if (code == 12004) {// false:踢出房间
				return JsonResponse.fail(ErrorCode.HAS_GET_OUT);
			} else if (code == 12005) {
				isShutUp = true;
			}
			long step4 = System.currentTimeMillis();
			// step:4 推送room信息
			roomService.pushRoom(uuid);
			// 获取关注关系
			Map<String, Object> reMap = roomService.getRoomInfo(uuid);
			reMap.put("isShutUp", isShutUp ? 1 : 0);
			boolean isFollow = userRelationService.exist(userId, parentStream.getUserId(), UserRelation.RelationType.FOLLOW.name());
//			boolean isFollow = userProxyService.isFollow(userId, parentStream.getUserId());
			reMap.put("isFollow", isFollow ? 1 : 0);
			Integer releaseCount = releaseProductService.selectCount(uuid);// 发布商品数量
			reMap.put("isReleasePro", releaseCount.intValue() > 0 ? 1 : 0);
			if (parentStream.getStatus() == StreamStatus.Publishing.ordinal()) {
//				Integer robCount = robService.getRobCount(parentStream.getUserId(), uuid);// 任务数量
				int robCount  =taskService.getRobCount(parentStream.getUserId(), uuid);
				reMap.put("isRob", robCount > 0 ? 1 : 0);
			}
			long step5 = System.currentTimeMillis();
			List<FavoriteItem<Product>> fList = favoriteService.getFavorites(userId, null, 10);
			if (fList == null || fList.isEmpty()) {
				reMap.put("isFavorited", 0);
			} else {
				reMap.put("isFavorited", 1);
			}

			long step6 = System.currentTimeMillis();
			logger.debug("inroom cost time: " + (step1 - start) + ", " + (step2 - step1) + ", " + (step3 - step2) + ", "
					+ (step4 - step3) + ", " + (step5 - step4) + ", " + (step6 - step5) + ", " + (step6 - start));
			return JsonResponse.success(reMap);
	}

	/**
	 * 退出房间<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/outroom", method = RequestMethod.POST)
	public ResponseEntity<Object> outroom(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = request.getHeader("userId");
		String uuid = body.getString("uuid");
		if (uuid == null) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		Stream stream = streamService.selectStream(uuid);
		if (stream == null) {// 判断主播是否存在
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		// step：1 获取房间新
		Room room = roomService.getRoom(uuid);
		if (room == null) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		List<String> closeStreramIds = new ArrayList<String>();

		// step:2 退出聊天室
		boolean isSuccess = chatRoomService.exitChatRoom(userId, room.getGroupId());
		if (!isSuccess) {// false:退出失败
			return JsonResponse.fail(ErrorCode.HAS_GET_OUT);
		}
		// step:3 添加观看时间
		RoomUser roomUser = roomService.getAudience(uuid, userId);
		if (roomUser != null && !roomUser.getUuid().equals(stream.getUserId())) {// 过滤主播对自己观看
			long watchTime = (long) ((new Date().getTime() - roomUser.getCreateTime().getTime()) / 1000);
			String watchStream = WatchType.WatchStream.name();
			if (stream.getStatus() == 2) {// 区分观众直播和回放
				watchStream = WatchType.WatchBack.name();
			}
			watchStreamService.save(userId, uuid, watchTime, watchStream);
		}
		Room childRoom = roomService.getChildRoom(userId, uuid);
		if (childRoom != null) {
			closeStreramIds.add(childRoom.getUuid());
		}
		// step：4 从观众和请求中表移除观众信息
		roomService.removeAudience(uuid, userId);// 从观众列表中移除
		roomService.removeChildRoom(userId, uuid);// 从连接列表中移除

		roomService.removeRequestConnect(uuid, userId);// 从请求列表中移除

		Iterable<Stream> childStreams = streamService.findConnectStream(userId, uuid);
		if (childStreams != null) {
			String groupId = room.getGroupId();
			for (Stream childStream : childStreams) {
				if (StringUtils.isNotBlank(groupId) && childStream.getStatus() == StreamStatus.Publishing.ordinal()) {
					// 消息推送到连接观众告诉主播已关闭
					Map<String, Object> extInfo = new HashMap<String, Object>();// 扩展信息
					extInfo.put("groupId", groupId);
					extInfo.put("childStream", childStream);
					extInfo.put("messageSource", Message.Source.ConnectingCancel.name());
					extInfo.put("from", "outroom1");
//					pushService.push(userId, groupId, JSONObject.toJSONString(extInfo));
					messageService.sendGroupMessage(groupId, "", extInfo, null);
				}
			}
		}

		if (closeStreramIds.size() > 0) {
			streamService.closeStreams(closeStreramIds);
		}
		// step：5 推送room信息
		roomService.pushRoom(uuid);
		return JsonResponse.success();
	}

	/**
	 * 观众列表<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/audience", method = RequestMethod.POST)
	public ResponseEntity<Object> audience(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String streamId = body.getString("uuid");
		if (streamId == null) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		String cursorId = body.getString("cursorId");
		cursorId = cursorId == null ? "" : cursorId;
		Integer pageCount = body.getInteger("pageSize");
		pageCount = pageCount == null ? 10 : pageCount;
		List<RoomUser> list = roomService.selectAudienceAll(request.getHeader("userId"),
				CommonUtil.getGroupId(streamId), streamId, cursorId, pageCount);
		List<Map<String, Object>> audiences = new ArrayList<Map<String, Object>>();
		for (RoomUser roomUser : list) {
			if (roomUser.getUuid().equals(request.getHeader("userId"))) {// 过滤主播信息
				continue;
			}
			audiences.add(roomUserToMap(roomUser));
		}
		return JsonResponse.success(audiences);
	}

	/**
	 * 更新观众和主播的关注<br>
	 */
	@Deprecated
	@ResponseBody
	@RequestMapping(value = "/follow", method = RequestMethod.POST)
	public ResponseEntity<Object> follow(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = request.getHeader("userId");// 观众
		String streamUserId = body.getString("userId");// 主播
		Integer follow = ConvertUtil.objectToInt(body.getInteger("follow"), false);// 0：取消关注
																					// 1：关注
		logger.info("关注变更:" + userId + "=" + streamUserId + "=" + follow);
		if (streamUserId != null) {
			List<Stream> streamList = streamService.selectOnLine(streamUserId);
			streamList.forEach((stream) -> {
				String streamId = stream.getUuid();
				RoomUser roomUser = roomService.getAudience(streamId, userId);
				if (roomUser != null) {
					roomUser.setIsFollow(follow);
					roomService.addAudience(roomUser);
				}
			});
		}
		return JsonResponse.success();
	}

	@ResponseBody
	@RequestMapping(value = "/forbiddenUser", method = RequestMethod.POST)
	public ResponseEntity<Object> forbiddenUser(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = body.getString("userId");
		List<Stream> list = streamService.userActiveStream(userId);
		if (list != null && !list.isEmpty()) {
			for (Stream s : list) {
				roomService.anchorClose(s.getUuid(), userId);
			}
		}
		return JsonResponse.success();
	}

	/**
	 * user信息转换成map<br>
	 * 
	 * @param user
	 * @return Map<String,Object>
	 */
	private Map<String, Object> userToMap(UserVo user) {
		Map<String, Object> userMap = new HashMap<String, Object>();
		Map<String, Object> profileMap = new HashMap<String, Object>();
		if (user == null) {
			userMap.put("uuid", "");
			userMap.put("isTrailer", 0);
			profileMap.put("nickname", "");
			profileMap.put("avatar", "");
			profileMap.put("level", 0);
		} else {
			userMap.put("uuid", user.getUuid());
			userMap.put("isTrailer", 0);
			UserProfileVo profile = user.getProfile();
			if (profile == null) {
				profileMap.put("nickname", "");
				profileMap.put("avatar", "");
				profileMap.put("level", 0);
			} else {
				profileMap.put("nickname", ConvertUtil.objectToString(profile.getNickname(), false));
				profileMap.put("avatar", ConvertUtil.objectToString(profile.getAvatar(), false));
				profileMap.put("level", ConvertUtil.objectToInt(profile.getLevel(), false));
			}
		}
		userMap.put("profile", profileMap);
		return userMap;
	}


	/**
	 * 创建房间观众roomUser信息<br>
	 * 
	 * @param userId
	 *            用户uuid
	 * @param uuid
	 *            直播uuid
	 * @return RoomUser
	 */
	private RoomUser createRoomUser(String userId, String streamId) {
		RoomUser roomUser = roomService.getRoomUser(userId);
		Room room = roomService.getRoom(streamId);
		if (room != null) {
			roomUser.setGroupId(room.getGroupId());
			String zbUserId = room.getRoomUser().getUuid();
			boolean isFollow = userRelationService.exist(userId, zbUserId, UserRelation.RelationType.FOLLOW.name());
//			boolean isFollow = userProxyService.isFollow(userId, zbUserId);// 查询是否关注主播
			roomUser.setIsFollow(isFollow ? 1 : 0);
		}
		return roomUser;
	}

	private Map<String, Object> roomUserToMap(RoomUser roomUser) {
		Map<String, Object> userMap = new HashMap<String, Object>();
		userMap.put("uuid", roomUser.getUuid());
		// userMap.put("isFollow", roomUser.getIsFollow());//是否关注主播
		userMap.put("isVip", roomUser.getIsVip());// 是否VIP
		Map<String, Object> profileMap = new HashMap<String, Object>();
		profileMap.put("nickname", ConvertUtil.objectToString(roomUser.getNickName(), false));
		profileMap.put("avatar", ConvertUtil.objectToString(roomUser.getAvatar(), false));
		userMap.put("profile", profileMap);
		return userMap;
	}
}
