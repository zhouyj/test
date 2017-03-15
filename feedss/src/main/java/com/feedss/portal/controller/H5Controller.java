package com.feedss.portal.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.Constants;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonData;
import com.feedss.base.JsonResponse;
import com.feedss.base.Pages;
import com.feedss.base.Row;
import com.feedss.base.util.DateUtil;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.contact.entity.Message.Source;
import com.feedss.contact.service.MessageService;
import com.feedss.content.entity.Stream;
import com.feedss.content.entity.Trailer;
import com.feedss.content.service.RoomService;
import com.feedss.content.service.StreamService;
import com.feedss.content.service.TrailerService;
import com.feedss.portal.common.Pager;
import com.feedss.portal.controller.domain.TaskParam;
import com.feedss.portal.dto.TaskDTO;
import com.feedss.portal.entity.Appointment;
import com.feedss.portal.entity.Interaction;
import com.feedss.portal.entity.Skill;
import com.feedss.portal.repository.AppointmentRepository;
import com.feedss.portal.service.RankService;
import com.feedss.portal.service.SkillService;
import com.feedss.portal.service.TaskService;
import com.feedss.portal.util.StringLength;
import com.feedss.user.entity.Profile;
import com.feedss.user.entity.UserRelation;
import com.feedss.user.entity.UserRelation.FollowType;
import com.feedss.user.entity.UserRelation.RelationType;
import com.feedss.user.model.UserProfileVo;
import com.feedss.user.model.UserRelationVO;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.ProfileService;
import com.feedss.user.service.SmsService;
import com.feedss.user.service.UserProductService;
import com.feedss.user.service.UserRelationService;
import com.feedss.user.service.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by shenbingtao on 2016/8/13.
 */
@Slf4j
@Controller
@RequestMapping("/h5")
public class H5Controller extends BaseController {

	@Autowired
	private TaskService taskService;

	@Autowired
	private SkillService skillService;

	@Autowired
	private ConfigureUtil configureUtil;

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private ProfileService profileService;

	@Autowired
	private UserService userService;

	@Autowired
	private StreamService streamService;

	@Autowired
	private SmsService smsService;
	
	@Autowired
	private RankService rankService;
	
	@Autowired
	private UserRelationService userRelationService;
	@Autowired
	private UserProductService userProductService;
	@Autowired
	private RoomService roomService;
	@Autowired
	private MessageService messageService; 
	@Autowired
	private TrailerService trailerService;

	@Resource
	RedisTemplate redisTemplate;

	private String RANK_CACHE = "portal:h5:rank:%s:%s:%s:%s:%s";

	private String USER_RANK_KEYLIST = "portal:h5:rank:userkeys:%s";

	@RequestMapping(value = "/welcome", method = { RequestMethod.GET, RequestMethod.POST })
	public String welcome(HttpServletRequest request, Map<String, Object> model) {
		model.put("time", new Date());
		model.put("message", "hello");
		return "welcome";
	}

	@RequestMapping(value = "/userAgreement", method = { RequestMethod.GET, RequestMethod.POST })
	public String userAgreement(HttpServletRequest request) {
		return "redirect:" + configureUtil.getConfig(Constants.AGREEMENT_URL);
	}

	@RequestMapping(value = "/report", method = { RequestMethod.GET, RequestMethod.POST })
	public String report(HttpServletRequest request, @RequestParam String userId, @RequestParam String toUserId,
			Map<String, Object> model) {
		model.put("userId", userId);
		model.put("toUserId", toUserId);

		model.put("userToken", userService.getUserToken(userId));
		return "/manage/user/report";
	}

	@RequestMapping(value = "/grade", method = { RequestMethod.GET, RequestMethod.POST })
	public String grade(HttpServletRequest request) {
		return "/user/grade";
	}

	@RequestMapping(value = "/followList", method = { RequestMethod.GET, RequestMethod.POST })
	public String followList(HttpServletRequest request, @RequestParam String viewUserId, @RequestParam String userId,
			@RequestParam(required = false, defaultValue = "") String nickname, Map<String, Object> model) {
		model.put("userId", userId);
		model.put("viewUserId", viewUserId);
		model.put("nickname", nickname + "的关注");

		model.put("userToken", userService.getUserToken(viewUserId));
		return "/user/follow";
	}

	@ResponseBody
	@RequestMapping(value = "/followListData", method = { RequestMethod.GET, RequestMethod.POST })
	public JsonData followListData(HttpServletRequest request, @RequestParam String viewUserId,
			@RequestParam String userId, @RequestParam(required = false) String cursorId,
			@RequestParam(required = false, defaultValue = "#{20}") Integer pageSize) {

		List<UserRelationVO> relations = userRelationService.userFollow(viewUserId, userId, 
				RelationType.FOLLOW.toString(), cursorId, pageSize);
		List<Row> list = getUserList(relations, false);
		Row rlt = new Row();
		rlt.put("viewUserId", viewUserId);
		rlt.put("userId", userId);
		if (list.size() > 0) {
			rlt.put("more", "true");
			rlt.put("cursorId", list.get(list.size() - 1).get("uuid"));
		}
		rlt.put("list", list);

		return JsonData.successNonNull(rlt);
	}

	@ResponseBody
	@RequestMapping(value = "/follow", method = { RequestMethod.GET, RequestMethod.POST })
	public JsonData follow(HttpServletRequest request, @RequestParam String userId, @RequestParam String toUserId) {
		UserRelation ur = userRelationService.add(userId, toUserId, RelationType.FOLLOW.name(), null);
		if(ur!=null){
			roomService.updateAudienceRelation(userId, toUserId, 1);
		}
		if (ur != null) {
			clearRankUserKeyList(userId);
			clearRankUserKeyList(toUserId);
		}
		return JsonData.fail(ErrorCode.SUCCESS);
	}

	@ResponseBody
	@RequestMapping(value = "/unFollow", method = { RequestMethod.GET, RequestMethod.POST })
	public JsonData unFollow(HttpServletRequest request, @RequestParam String userId, @RequestParam String toUserId) {
		
		userRelationService.del(toUserId, userId, RelationType.FOLLOW.toString());
		roomService.updateAudienceRelation(toUserId, userId, FollowType.UNFOLLOW.ordinal());
		clearRankUserKeyList(userId);
		clearRankUserKeyList(toUserId);
		return JsonData.fail(ErrorCode.SUCCESS);
	}

	private void clearRankUserKeyList(String userId) {
		// success
		String key = String.format(USER_RANK_KEYLIST, userId);
		Set<String> set = redisTemplate.opsForSet().members(key);
		if (set != null) {
			redisTemplate.delete(set);
		}
	}

	@RequestMapping(value = "/fansList", method = { RequestMethod.GET, RequestMethod.POST })
	public String fansList(HttpServletRequest request, @RequestParam String viewUserId, @RequestParam String userId,
			@RequestParam(required = false, defaultValue = "") String nickname, Map<String, Object> model) {
		model.put("userId", userId);
		model.put("viewUserId", viewUserId);
		model.put("nickname", nickname + "的粉丝");

		model.put("userToken", userService.getUserToken(viewUserId));
		return "/user/fans";
	}

	@ResponseBody
	@RequestMapping(value = "/fansListData", method = { RequestMethod.GET, RequestMethod.POST })
	public JsonData fansListData(HttpServletRequest request, @RequestParam String viewUserId, @RequestParam String userId,
			@RequestParam(required = false) String cursorId,
			@RequestParam(required = false, defaultValue = "#{20}") Integer pageSize) {
		List<UserRelationVO> result = userRelationService.userFollowBy(viewUserId, userId, 
				RelationType.FOLLOW.toString(), cursorId, pageSize);
		List<Row> list = getUserList(result, true);
		
		Row data = new Row();
		data.put("viewUserId", viewUserId);
		data.put("userId", userId);

		if (list.size() > 0) {
			data.put("more", "true");
			data.put("cursorId", list.get(list.size() - 1).gets("uuid"));
		}
		data.put("list", list);

		return JsonData.successNonNull(data);
	}
	
	private List<Row> getUserList(List<UserRelationVO> list, boolean isFans){
		List<Row> result = new ArrayList<Row>();
		if (list == null || list.isEmpty()) {
			return result;
		}
		for (int i = 0; i < list.size(); i++) {
			UserRelationVO one = list.get(i);
			UserVo user = one.getToUser();
			if(isFans){
				user = one.getFromUser();
			}
			
			Row row = new Row();
			row.put("fromUserId", one.getFromUserId());
			row.put("toUserId", one.getToUserId());
			row.put("follow", one.getFollow());
			row.put("followBy", one.getFollowBy());
			UserProfileVo profile = user.getProfile();
			row.put("nickname", profile.getNickname());
			row.put("nicknameShort", getShortNickname(profile.getNickname()));
			row.put("avatar", profile.getAvatar());
			row.put("uuid", one.getUuid());
			result.add(row);
		}
		return result;
	}

	// 礼物列表
	@RequestMapping(value = "/myGifts", method = { RequestMethod.GET, RequestMethod.POST })
	public String myGifts(HttpServletRequest request, @RequestParam String userId, Map<String, Object> model) {
		List<HashMap<String,Object>> productVos =userProductService.getProductsByUserId(userId);
		model.put("list", productVos == null ? new ArrayList<>() : productVos);
		model.put("userId", userId);
		return "/user/gift";
	}

	private String getShortNickname(String nickname) {
		if (StringUtils.isNotEmpty(nickname) && StringLength.getLength(nickname) > 8) {
			return nickname.substring(0, 8) + "...";
		}
		return nickname;
	}

	// 排行榜，取前10条
	@RequestMapping(value = "/rankList", method = { RequestMethod.GET, RequestMethod.POST })
	public String rankList(HttpServletRequest request, @RequestParam Integer category, @RequestParam Integer type,
			@RequestParam(required = false, defaultValue = "#{1}") Integer pageNo,
			@RequestParam(required = false, defaultValue = "#{10}") Integer pageSize, @RequestParam String viewUserId,
			Map<String, Object> model) {
		Map<String, Object> renfo = null;
		String key = String.format(RANK_CACHE, category, type, pageNo, pageSize, viewUserId);
		if (!redisTemplate.hasKey(key)) {
			if (category.intValue() != 1) {
				renfo = rankService.ranking(category, type, viewUserId, pageNo, pageSize);
			} else {
				renfo = userRelationService.xRank(viewUserId, type, pageNo, pageSize);// 查询人气榜
			}
			if (renfo != null && renfo.containsKey("list") && !((List)renfo.get("list")).isEmpty()) {
				redisTemplate.opsForValue().set(key, renfo);
				redisTemplate.expire(key, 10, TimeUnit.MINUTES);
				String listKey = String.format(USER_RANK_KEYLIST, viewUserId);
				redisTemplate.opsForSet().add(listKey, key);
			}
		}else{
			renfo = (Map<String, Object>) redisTemplate.opsForValue().get(key);
		}
		if(renfo!=null){
			model.putAll(renfo);
		}
		
		String url = "/h5/rankList?category=" + category + "&pageNo=" + pageNo + "&pageSize=" + pageSize
				+ "&viewUserId=" + viewUserId;
		model.put("viewUserId", viewUserId);
		model.put("weekUrl", url + "&type=1");
		model.put("monthUrl", url + "&type=2");
		model.put("yearUrl", url + "&type=3");
		model.put("category", category);
		model.put("type", type);
		model.put("nextPage", pageNo + 1);

		model.put("userToken", userService.getUserToken(viewUserId));
		return "/user/rank_list";
	}

	@RequestMapping(value = "/task/publishList", method = { RequestMethod.GET })
	public String taskPublishList(HttpServletRequest request, @RequestParam String userId, Map<String, Object> model) {

		List<TaskDTO> list = taskService.getPublishTaskList(userId, null);

		model.put("list", list);
		model.put("userId", userId);

		model.put("userToken", userService.getUserToken(userId));
		return "/task/publish_list";
	}

	@RequestMapping(value = "/task/robList", method = { RequestMethod.GET })
	public String taskRobList(HttpServletRequest request, @RequestParam String userId, Map<String, Object> model) {

		List<TaskDTO> list = taskService.getAcceptTaskList(userId, null);
		log.debug("robList, userId = " + userId + ", list = " + JSONObject.toJSONString(list));
		model.put("list", list);
		model.put("userId", userId);

		model.put("userToken", userService.getUserToken(userId));
		return "/task/rob_list";
	}

	@RequestMapping(value = "/skillList", method = { RequestMethod.GET })
	public String skillList(HttpServletRequest request, @RequestParam String viewUserId, @RequestParam String userId,
			@RequestParam String type, @RequestParam(required = false, defaultValue = "#{1}") Integer pageNo,
			@RequestParam(required = false, defaultValue = "#{10}") Integer pageSize, Map<String, Object> model) {

		viewUserId = viewUserId.trim();
		userId = userId.trim();
		Pager pager = new Pager(pageNo, pageSize);
		List<Skill> list = skillService.getContentList(userId, type, pager);
		log.info("skillList====data list:" + JSON.toJSONString(list));

		model.put("list", list);
		model.put("type", type);
		model.put("viewUserId", viewUserId);// 查看者
		model.put("userId", userId);// 查看者
		model.put("pageNo", pageNo + 1);
		model.put("more", pager.hasNextPage() ? "true" : "false");
		model.put("url", "/h5/skillList?userId=" + userId + "&viewUserId=" + viewUserId);// 被查看者

		model.put("userToken", userService.getUserToken(userId));
		return "/skill/list";
	}

	@ResponseBody
	@RequestMapping(value = "/skillListData", method = { RequestMethod.GET })
	public JsonData skillListData(HttpServletRequest request,
			@RequestParam(required = false, defaultValue = "0") String viewUserId, @RequestParam String userId,
			@RequestParam String type, @RequestParam Integer pageNo,
			@RequestParam(required = false, defaultValue = "#{10}") Integer pageSize) {

		Pager pager = new Pager(pageNo, pageSize);
		List<Skill> list = skillService.getContentList(userId, type, pager);
		if (list == null) {
			list = new ArrayList<Skill>();
		}
		Row data = new Row();
		data.put("list", list);
		data.put("pageNo", pageNo + 1);
		data.put("more", pager.hasNextPage() ? "true" : "false");
		return JsonData.successNonNull(data);
	}

	@ResponseBody
	@RequestMapping(value = "/count", method = { RequestMethod.POST })
	public JsonData count(HttpServletRequest request, @RequestBody String msgStr) {
		TaskParam param = JSON.parseObject(msgStr, TaskParam.class);
		int skillCount = skillService.getTotalCount(param.getUserId());
		Row data = new Row();
		data.put("skillCount", skillCount);
		return JsonData.successNonNull(data);
	}
	
	@RequestMapping(value = "/trailer", method = { RequestMethod.GET })
	public String shareTrailer(HttpServletRequest request, @RequestParam(required = true) String id, Map<String, Object> model) {
		Trailer t = trailerService.findByUuid(id);
		if(t==null){
			return "/trailer";
		}
		if(StringUtils.isNotEmpty(t.getStreamId())){
			return share(request, t.getStreamId(), model);
		}
		
		String iosAppId = configureUtil.getConfig("iosAppId");
		model.put("appleAppContent", "app-id=" + iosAppId + ", affiliate-data=myAffiliateData, app-argument=");
		
		String appName = configureUtil.getConfig(Constants.APP_NAME);
		model.put("appName", appName);
		
		UserVo user = userService.getUserVoByUserId(t.getUserId());

		model.put("title", t.getTitle());
		model.put("picUrl", t.getPicUrl());
		if (user != null) {
			model.put("avatar", user.getProfile().getAvatar());
			model.put("nickname", user.getProfile().getNickname());
		}
		Date playTime = t.getPlayTime();
		model.put("playTime", DateUtil.dateToString(playTime, DateUtil.FORMAT_MINUTE_CN));
		model.put("content", t.getContent());

		model.put("appDownloadUrl", configureUtil.getConfig(Constants.DOWNLOAD_URL));
		return configureUtil.getConfig("template_h5") + "/trailer";
	}

	@RequestMapping(value = "/share", method = { RequestMethod.GET })
	public String share(HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") String streamId, Map<String, Object> model) {
		String template = configureUtil.getConfig("template_h5") + "/stream";
		
		Stream stream = streamService.selectStream(streamId);
		if(stream==null){
			return template;
		}
		streamService.updatePlayCount(streamId, 1);
		
		String iosAppId = configureUtil.getConfig("iosAppId");
		model.put("appleAppContent", "app-id=" + iosAppId + ", affiliate-data=myAffiliateData, app-argument=");
		
		String appName = configureUtil.getConfig(Constants.APP_NAME);
		model.put("appName", appName);
		
		UserVo user = userService.getUserVoByUserId(stream.getUserId());

		model.put("videoTitle", stream.getName());
		model.put("videoCoverUrl", stream.getCover());
		if (user != null) {
			model.put("avatar", user.getProfile().getAvatar());
			model.put("nickname", user.getProfile().getNickname());
		}
		model.put("playCount", stream.getPlayCount());
		model.put("h5Footer", configureUtil.getConfig(Constants.H5_FOOTER));
		model.put("appDownloadUrl", configureUtil.getConfig(Constants.DOWNLOAD_URL));
		model.put("status", stream.getStatus());
		if(stream.isDeleted()){
			model.put("status", -1);
			return template;
		}
		if(StringUtils.isNoneBlank(stream.getSecret())){
			model.put("isPrivate", 1);
			return template;
		}
		model.put("content", configureUtil.getConfig("shareDesc"));
		if(stream.getStatus()==Stream.StreamStatus.Created.ordinal()){
			return template;
		}else if(stream.getStatus()==Stream.StreamStatus.Publishing.ordinal()){
			model.put("videoUrl", stream.getHlsUri());
		}else if(stream.getStatus()==Stream.StreamStatus.Ended.ordinal()){
			// h5 回放地址为mp4
			String url = stream.getPlaybackUri();
			StringBuffer sb = new StringBuffer();
			if(StringUtils.isNotEmpty(url)){
				String[] urls = url.split(",");
				for(String s:urls){
					if(s.endsWith("flv")){
						s = s.substring(0, s.lastIndexOf("flv")) + "mp4";
						if(sb.length()>0){
							sb.append("," + s);
						}else{
							sb.append(s);
						}
					}else{
						sb.append(s);
					}
				}
			}
			
			model.put("videoUrl", sb.toString());
		}
		return template;
	}
	
	@RequestMapping(value = "/shareXinhua", method = { RequestMethod.GET })
	public String shareXinhua(HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") String streamId, Map<String, Object> model) {
		String template = "/xinhuanet/stream";
		
		Stream stream = streamService.selectStream(streamId);
		if(stream==null){
			return template;
		}
		streamService.updatePlayCount(streamId, 1);
		
		String iosAppId = configureUtil.getConfig("iosAppId");
		model.put("appleAppContent", "app-id=" + iosAppId + ", affiliate-data=myAffiliateData, app-argument=");
		
		String appName = configureUtil.getConfig(Constants.APP_NAME);
		model.put("appName", appName);
		
		UserVo user = userService.getUserVoByUserId(stream.getUserId());

		model.put("videoTitle", stream.getName());
		model.put("videoCoverUrl", stream.getCover());
		if (user != null) {
			model.put("avatar", user.getProfile().getAvatar());
			model.put("nickname", user.getProfile().getNickname());
		}
		model.put("playCount", stream.getPlayCount());
		model.put("appDownloadUrl", configureUtil.getConfig(Constants.DOWNLOAD_URL));
		model.put("status", stream.getStatus());
		if(stream.isDeleted()){
			model.put("status", -1);
			return template;
		}
		if(StringUtils.isNoneBlank(stream.getSecret())){
			model.put("isPrivate", 1);
			return template;
		}
		model.put("content", configureUtil.getConfig("shareDesc"));
		if(stream.getStatus()==Stream.StreamStatus.Created.ordinal()){
			return template;
		}else if(stream.getStatus()==Stream.StreamStatus.Publishing.ordinal()){
			model.put("videoUrl", stream.getHlsUri());
		}else if(stream.getStatus()==Stream.StreamStatus.Ended.ordinal()){
			// h5 回放地址为mp4
			String url = stream.getPlaybackUri();
			StringBuffer sb = new StringBuffer();
			if(StringUtils.isNotEmpty(url)){
				String[] urls = url.split(",");
				for(String s:urls){
					if(s.endsWith("flv")){
						s = s.substring(0, s.lastIndexOf("flv")) + "mp4";
						if(sb.length()>0){
							sb.append("," + s);
						}else{
							sb.append(s);
						}
					}else{
						sb.append(s);
					}
				}
			}
			
			model.put("videoUrl", sb.toString());
		}
		return template;
	}
	

	@RequestMapping(value = "/sharePrivate", method = { RequestMethod.GET })
	public String sharePrivate(HttpServletRequest request,
			@RequestParam(required = false, defaultValue = "") String videoTitle,
			@RequestParam(required = false, defaultValue = "") String videoCoverUrl,
			@RequestParam(required = false, defaultValue = "") String avatar,
			@RequestParam(required = false, defaultValue = "") String nickname,
			@RequestParam(required = false, defaultValue = "") String userCount,
			@RequestParam(required = false, defaultValue = "0") String streamId, Map<String, Object> model) {
		return share(request, streamId, model);
	}

	@RequestMapping(value = "/blank", method = { RequestMethod.GET })
	public String sharePrivate(HttpServletRequest request) {
		return "/blank";
	}

	@RequestMapping(value = "/appDownload2", method = { RequestMethod.GET })
	public String appDownload2(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent") != null ? request.getHeader("User-Agent").toLowerCase() : "";
		if (userAgent.indexOf("android") >= 0) {
			return "redirect:" + configureUtil.getConfig(Constants.ANDROID_URL);
		} else {
			return "redirect:" + configureUtil.getConfig(Constants.IOS_URL);
		}
	}

	@RequestMapping(value = "/appDownload", method = { RequestMethod.GET })
	public String appDownload(HttpServletRequest request, Map<String, Object> model) {
		model.put("androidUrl", configureUtil.getConfig(Constants.ANDROID_URL));
		model.put("iosUrl", configureUtil.getConfig(Constants.IOS_URL));
		return configureUtil.getConfig("template_h5") + "/app_download";
	}

	// 礼物列表
	@RequestMapping(value = "/myGiftsShare", method = { RequestMethod.GET, RequestMethod.POST })
	public String myGiftsShare(HttpServletRequest request, @RequestParam String userId, Map<String, Object> model) {
		List<HashMap<String,Object>> productVos =userProductService.getProductsByUserId(userId);

		model.put("list", productVos);
		model.put("userId", userId);
		model.put("appDownloadUrl", configureUtil.getConfig(Constants.DOWNLOAD_URL));
		return "/user/gift2";
	}

	@RequestMapping(value = "/ruleForInteraction", method = { RequestMethod.GET, RequestMethod.POST })
	public String ruleForInteraction(HttpServletRequest request, @RequestParam String type, Map<String, Object> model) {
		String title = "";
		String content = "";
		if (Interaction.InteractionType.BonusTask.toString().equals(type)) {
			title = "任务规则说明";
			content = configureUtil.getConfig("rule_content_for_BonusTask");
		} else if (Interaction.InteractionType.Bid.toString().equals(type)) {
			title = "投标规则说明";
			content = configureUtil.getConfig("rule_content_for_Bid");
		} else {
			title = content = "传入type字段错误.";
		}
		model.put("title", title);
		model.put("content", content);
		return configureUtil.getConfig("template_h5") + "/rules";
	}

	@RequestMapping(value = "/appointment", method = RequestMethod.GET)
	public String appointmentGet(HttpServletRequest request, @RequestParam(required = true) String type,
			@RequestParam(required = true) String userId, @RequestParam(required = true) String targetUserId,
			Map<String, Object> model) {
		model.put("type", type);
		if ("huiketing".equals(type)) {
			model.put("title", "预约观看会客厅");
			model.put("imageURL", "/image/huiketing.png");
		} else if ("yunnvjia".equals(type)) {
			model.put("title", "申请参观云女家");
			model.put("imageURL", "/image/yunnvjia.png");
		} else {
			model.put("title", "type参数错误.");
		}
		model.put("userId", userId);
		model.put("targetUserId", targetUserId);
		Profile profile = profileService.findByUserId(targetUserId);
		model.put("targetUserNickname", profile.getNickname());
		model.put("avatar", profile.getAvatar());
		return configureUtil.getConfig("template_h5") + "/appointmentForm";
	}

	@RequestMapping(value = "/appointment", method = RequestMethod.POST)
	public ResponseEntity<Object> appointmentPost(HttpServletRequest request,
			@RequestParam(required = true) String type, @RequestParam(required = true) String userId,
			@RequestParam(required = true) String targetUserId, @RequestParam(required = true) String mobile,
			Map<String, Object> model) {
		Appointment appointment = new Appointment();
		appointment.setType(type);
		appointment.setUserId(userId);
		appointment.setTargetUserId(targetUserId);
		appointment.setMobile(mobile);
		appointmentRepository.save(appointment);

		String title = "";
		if ("huiketing".equals(type)) {
			title = "会客厅";
		} else if ("yunnvjia".equals(type)) {
			title = "云女家";
		}
		String thanksForVisitSmsContent = configureUtil.getConfig("thanksForVisitSmsContent");
		thanksForVisitSmsContent = thanksForVisitSmsContent.replaceAll("title", title);
		thanksForVisitSmsContent = thanksForVisitSmsContent.replaceAll("mobile", mobile);
		smsService.sendSms(mobile, thanksForVisitSmsContent);
		//给主播发系统消息
		Map<String, Object> ext = new HashMap<>();
		ext.put("messageSource", Source.Appointment.name());
		ext.put("userId", userId);
		String content  = configureUtil.getConfig("appointmentNoticeTemplate");
		content = content.replaceAll("title", title).replaceAll("nickname", userService.getUserVoByUserId(userId).getProfile().getNickname()).replaceAll("mobile", mobile);
		messageService.sendSystemMessage(content, ext, targetUserId);
		
		return JsonResponse.success(appointment);
	}

	@RequestMapping(value = "/profile", method = { RequestMethod.GET, RequestMethod.POST })
	public String profile(HttpServletRequest request, @RequestParam String userId, Map<String, Object> model) {

		String appName = configureUtil.getConfig(Constants.APP_NAME);
		model.put("appName", appName);
		
		Profile profile = profileService.findByUserId(userId);
		model.put("profile", profile);

		int totalStreamTime = streamService.getTotalStreamTime(userId);
		model.put("totalStreamTime", totalStreamTime);
		model.put("friendlyTotalStreamTime", DateUtil.formatDuration(totalStreamTime));

		JSONObject space = userService.space(userId, userId, 0);
		model.put("space", space);

		Pages<Stream> pages = streamService.findStreams(userId, 0, 10);
		List<Stream> streamList = pages.getList();
		List<Map<String, Object>> contentList = streamService.getStreams(streamList, true);
		model.put("contentList", contentList);
		return configureUtil.getConfig("template_h5") + "/profile";
	}

}
