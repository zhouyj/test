package com.feedss.user.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feedss.app.ButtonService;
import com.feedss.base.Constants;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.Pages;
import com.feedss.base.util.DateUtil;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.content.entity.Banner;
import com.feedss.content.entity.Stream;
import com.feedss.content.entity.Trailer;
import com.feedss.content.service.BannerService;
import com.feedss.content.service.CategoryService;
import com.feedss.content.service.StreamService;
import com.feedss.content.service.TrailerService;
import com.feedss.portal.service.InteractionService;
import com.feedss.user.controller.SmsController.SmsType;
import com.feedss.user.entity.Apply;
import com.feedss.user.entity.Apply.ApplyAction;
import com.feedss.user.entity.Configure;
import com.feedss.user.entity.Profile;
import com.feedss.user.entity.Role;
import com.feedss.user.entity.TimeCard;
import com.feedss.user.entity.TimeCard.TimeCardStatus;
import com.feedss.user.entity.User;
import com.feedss.user.entity.User.UserStatus;
import com.feedss.user.entity.UserRelation.RelationType;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.AccountService;
import com.feedss.user.service.ApplyService;
import com.feedss.user.service.LogService;
import com.feedss.user.service.OfflineService;
import com.feedss.user.service.ProfileService;
import com.feedss.user.service.RoleService;
import com.feedss.user.service.UserFavoriteService;
import com.feedss.user.service.UserRelationService;
import com.feedss.user.service.UserRoleService;
import com.feedss.user.service.UserSearchService;
import com.feedss.user.service.UserService;

@RestController
@RequestMapping("user")
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	ProfileService profileService;

	@Autowired
	ApplyService applyService;

	@Autowired
	RoleService roleService;

	@Autowired
	LogService logService;

	@Autowired
	OfflineService offlineService;

	@Autowired
	UserRelationService userRelationService;

	@Autowired
	AccountService accountService;

	@Autowired
	UserSearchService searchService;

	@Autowired
	UserFavoriteService favoriteService;

	@Autowired
	UserRoleService userRoleService;

	@Autowired
	StreamService streamService;
	
	@Autowired
	InteractionService interactionService;
	@Autowired
	private ButtonService buttonService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private BannerService bannerService;
	@Autowired
	private TrailerService trailerService;

	@Autowired
	ConfigureUtil configureUtil;

	Log logger = LogFactory.getLog(getClass());
	
	/**
	 * 用户鉴权
	 * 
	 * @param token
	 * @return
	 */
	@RequestMapping(value = "verifyToken", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> verifyToken(HttpServletRequest request, @RequestBody String body) {
		JSONObject json = JSONObject.parseObject(body);
		String token = json.getString("userToken");
		String userId = json.getString("userId");
		UserVo userVo = userService.getUserVoByUserId(userId);
		if (userVo == null)
			return JsonResponse.fail(ErrorCode.USER_NOT_EXIST);
		else if (userVo.getStatus() == UserStatus.DISABLED.ordinal())
			return JsonResponse.fail(ErrorCode.USER_FORBIDDEN);
		Map<String, Object> data = new HashMap<String, Object>();
		String _token = userService.getUserToken(userId);
		if (_token != null && _token.equals(token)) {
			data.put("uuid", userId);
			return JsonResponse.success(data);
		} else {
			return JsonResponse.fail(ErrorCode.INVALID_TOKEN);
		}
	}

	/**
	 * 用户信息
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "profile", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> profile(HttpServletRequest request, @RequestBody String body,
			@RequestHeader String userId) {
		JSONObject json = JSONObject.parseObject(body);
		String toUserId = json.getString("userId");
		Map<String, Object> data = userService.getProfile(userId, toUserId);
		
		int totalIncome = 0;
		String streamId = json.getString("streamId");
		
		if(StringUtils.isNotEmpty(streamId)){
			totalIncome = interactionService.getTotalIncome(streamId);
			data.put("totalIncome", totalIncome);
		}
		
		
		return JsonResponse.success(data);
	}

	/**
	 * 6.10 top by followed
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "topByFollowed", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> topByFollowed(HttpServletRequest request, @RequestBody String body,
			@RequestHeader String userId) {
		JSONObject json = JSONObject.parseObject(body);
		Integer pageSize = json.getInteger("pageSize");// 每页显示数量
		pageSize = pageSize == null ? 10 : pageSize;
		String directionStr = json.getString("direction");// 翻页反向 next：下页 pre：上页
		String cursorId = json.getString("cursorId");// 当前游标

		int direction = 1;// 1：更新 2：下一页
		if (StringUtils.isEmpty(directionStr) || directionStr.equals("next")) {
			direction = 2;
		}
		List<UserVo> userList = userService.getHostList(userId, cursorId, pageSize, direction);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("list", userList);
		return JsonResponse.success(data);
	}

	/**
	 * 1.5编辑个人信息
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "editProfile", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> editProfile(HttpServletRequest request, @RequestBody String body,
			@RequestHeader String userId, @RequestHeader String userToken) {
		JSONObject json = JSONObject.parseObject(body);
		return JsonResponse.response(userService.updateProfile(userId, json));
	}

	/**
	 * 用户信息(批量)
	 * 
	 * @param
	 * @return
	 */
	@RequestMapping(value = "batchProfile", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> batchProfile(HttpServletRequest request, @RequestBody String body) {
		JSONObject json = JSONObject.parseObject(body);
		JSONArray userIds = json.getJSONArray("userIds");
		String[] userId = userIds.toArray(new String[0]);

		Map<String, Object> data = new HashMap<String, Object>();
		/* List<User> users=userService.batchUser(userId); */
		List<UserVo> userVos = new ArrayList<UserVo>();
		for (String id : userId) {
			UserVo userVo = userService.getUserVoByUserId(id);
			if (userVo != null) {
				userVos.add(userVo);
			}
		}
		if (userVos.size() > 0) {
			data.put("list", userVos);
		}
		return JsonResponse.success(data);
	}

	/**
	 * 用户帐号密码登录
	 * 
	 * @return
	 */
	@RequestMapping(value = "pwdLogin", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> pwdLogin(HttpServletRequest request, @RequestBody String body,
			@RequestHeader("deviceAgent") String deviceAgent) {
		JSONObject json = JSONObject.parseObject(body);
		String username = json.getString("username");
		String password = json.getString("password");

		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
			return JsonResponse.fail(ErrorCode.NEED_USERNAME_AND_PASSWORD);
		}
		return JsonResponse.response(userService.pwdLogin(username, password, deviceAgent));
	}

	/**
	 * 用户注册
	 * 
	 * @param mobile
	 * @param smsCode
	 * @return
	 */
	@RequestMapping(value = "checkTimeCard", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> checkTimeCard(HttpServletRequest request, @RequestBody String body) {
		JSONObject json = JSONObject.parseObject(body);
		String serialNumber = json.getString("serialNumber");
		String password = json.getString("password");

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("serialNumber", serialNumber);
		TimeCard timeCard = accountService.getTimeCard(serialNumber, password);
		if (timeCard == null) {
			return JsonResponse.fail(ErrorCode.CARD_OR_PASSROD_ERROR);
		} else if (timeCard.getStatus() == TimeCardStatus.ACTIVIED.ordinal()) {
			return JsonResponse.fail(ErrorCode.CARD_ACTIVED);
		} else if (timeCard.getStatus() == TimeCardStatus.INVALID.ordinal()) {
			return JsonResponse.fail(ErrorCode.INVALIDE_CARD);
		}
		return JsonResponse.success(data);
	}

	/**
	 * 学习卡绑定
	 * 
	 * @param
	 * @param
	 * @return
	 */
	@RequestMapping(value = "bindTimeCard", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> bindTimeCard(HttpServletRequest request, @RequestBody String body,
			@RequestHeader("userId") String userId) {
		JSONObject json = JSONObject.parseObject(body);
		String serialNumber = json.getString("serialNumber");
		String password = json.getString("password");
		
		return JsonResponse.response(userService.bindTimecard(userId, serialNumber, password));
	}

	/**
	 * 用户注册
	 * 
	 * @param mobile
	 * @param smsCode
	 * @return
	 */
	@RequestMapping(value = "registerByMobile", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> registerByMobile(HttpServletRequest request, @RequestBody String body) {
		JSONObject json = JSONObject.parseObject(body);
		String mobile = json.getString("mobile");
		String smsCode = json.getString("smsCode");
		String pwd = json.getString("pwd");
		return JsonResponse.response(userService.registerByMobile(mobile, smsCode, pwd));
	}

	/**
	 * 用户注册
	 * 
	 * @param mobile
	 * @param smsCode
	 * @return
	 */
	@RequestMapping(value = "registerByUsername", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> registerByUsername(HttpServletRequest request, @RequestBody String body) {
		JSONObject json = JSONObject.parseObject(body);
		String username = json.getString("username");
		String pwd = json.getString("pwd");
		String timecardNumber = json.getString("timecardNumber");
		return JsonResponse.response((userService.registerByUsername(username, pwd, timecardNumber)));
	}

	@RequestMapping(value = "resetPwd", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> resetPwd(HttpServletRequest request, @RequestBody String body) {
		JSONObject json = JSONObject.parseObject(body);
		String mobile = json.getString("mobile");
		String smsCode = json.getString("smsCode");
		String pwd = json.getString("pwd");
		String pwd2 = json.getString("pwd2");
		return JsonResponse.response(userService.resetPwd(mobile, smsCode, pwd, pwd2));
	}

	@RequestMapping(value = "updatePwd", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> updatePwd(HttpServletRequest request, @RequestBody String body,
			@RequestHeader("userId") String userId) {
		JSONObject json = JSONObject.parseObject(body);
		String oldPwd = json.getString("oldPwd");
		String pwd = json.getString("pwd");
		String pwd2 = json.getString("pwd2");
		return JsonResponse.response(userService.updatePwd(userId, oldPwd, pwd, pwd2));
	}

	/**
	 * 用户登录
	 * 
	 * @param mobile
	 * @param smsCode
	 * @return
	 */
	@RequestMapping(value = "login", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> login(HttpServletRequest request, @RequestBody String body,
			@RequestHeader("deviceId") String deviceId, @RequestHeader("deviceAgent") String deviceAgent) {
		JSONObject json = JSONObject.parseObject(body);
		String mobile = json.getString("mobile");
		String smsCode = json.getString("smsCode");

		long start = System.currentTimeMillis();
		Map<String, Object> data = new HashMap<String, Object>();
		boolean result = userService.checkVerifyCode(mobile, smsCode, SmsType.LOGIN);
		if (result) {

			long step1 = System.currentTimeMillis();
			User u = userService.findByMobile(mobile);
			UserVo userVo = new UserVo();
			if (u == null) {
				u = new User();
				u.setMobile(mobile);
				userVo = userService.create(u);
				data.put("firsttime", 1);
			    if(logger.isDebugEnabled()){
			    	logger.debug("register, login cost time: " + (step1-start) + ", " + (System.currentTimeMillis()-step1));
			    }
			} else {
				if (u.getStatus() == 1) {
					return JsonResponse.fail(ErrorCode.USER_FORBIDDEN);// 用户被禁用
				}
				data.put("firsttime", 0);
				userVo = userService.loginUser(u.getUuid());
				userService.checkDevice(u.getUuid(), deviceId, deviceAgent);
				if(logger.isDebugEnabled()){
			    	logger.debug("login cost time: " + (step1-start) + ", " + (System.currentTimeMillis()-step1));
			    }
			}
			
			data.put("user", userVo);
			return JsonResponse.successNonNull(data);
		} else {
			return JsonResponse.fail(ErrorCode.INVALIDE_CODE);
		}
	}

	/**
	 * 用户登录(第三方)
	 * 
	 * @param mobile
	 * @param smsCode
	 * @return
	 */
	@RequestMapping(value = "thirdpartyLogin", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> thirdpartyLogin(HttpServletRequest request, @RequestBody String body,
			@RequestHeader("deviceId") String deviceId, @RequestHeader("deviceAgent") String deviceAgent) {
		JSONObject json = JSONObject.parseObject(body);
		String thirdpartyId = json.getString("thirdpartyId");
		String thirdpartyName = json.getString("thirdpartyName");
		String avatar = json.getString("avatar");
		String nickname = json.getString("nickname");
		int gender = json.getInteger("gender") == null ? 0 : json.getInteger("gender");

		Map<String, Object> data = new HashMap<String, Object>();
		User u = userService.findByThirdparty(thirdpartyName, thirdpartyId);
		UserVo userVo = null;
		if (u == null) {
			u = new User();
			u.setThirdpartyId(thirdpartyId);
			u.setThirdpartyName(thirdpartyName);
			Profile p = new Profile();
			p.setAvatar(avatar);
			p.setNickname(nickname);
			p.setGender(gender);
			p.setUserId(u.getUuid());
			u.setProfile(p);
			userVo = userService.create(u);
			data.put("firsttime", 1);
			profileService.updateProfile2Connect(p, userVo.getUuid()); // 更新用户信息到消息中心
		} else {
			// 验证设备变化

			if (u.getStatus() == 1) {
				return JsonResponse.fail(ErrorCode.USER_FORBIDDEN);// 用户被禁用
			}
			userVo = userService.loginUser(u.getUuid());
			data.put("firsttime", 0);
		}
		userService.checkDevice(u.getUuid(), deviceId, deviceAgent);
		data.put("user", userVo);
		return JsonResponse.successNonNull(data);
	}

	/**
	 * 接口1.3绑定手机号
	 * 
	 * @param mobile
	 * @param smsCode
	 * @return
	 */
	@RequestMapping(value = "bindMobile", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> bindMobile(HttpServletRequest request, @RequestBody String body,
			@RequestHeader String userId) {
		JSONObject json = JSONObject.parseObject(body);
		String mobile = json.getString("mobile");
		String smsCode = json.getString("smsCode");
		return JsonResponse.response(userService.bindMobile(userId, mobile, smsCode));
	}

	/**
	 * 处理一些行为
	 * 
	 * @param request
	 * @param body
	 * @return
	 */
	@RequestMapping(value = "addLog", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> addLog(HttpServletRequest request, @RequestBody String body) {
		JSONObject json = JSONObject.parseObject(body);
		String userId = json.getString("userId");
		String object = json.getString("object");
		String objectId = json.getString("objectId");
		String type = json.getString("type");
		String extAttr = json.getString("extAttr");
		return JsonResponse.response(userService.addLog(userId, object, objectId, type, extAttr));
	}

	/**
	 * 角色列表
	 */
	@RequestMapping(value = "roleList", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> roleList(HttpServletRequest request, @RequestBody String body) {
		Map<String, Object> data = new HashMap<String, Object>();
		List<Role> list = roleService.roleList();
		data.put("list", list);
		return JsonResponse.successNonNull(data);
	}

	/**
	 * 申请认证
	 * 
	 * @param userId
	 *            用户id
	 * @param reason
	 *            原因
	 * @param roleId
	 *            角色id
	 * @return
	 */
	@RequestMapping(value = "applyRole", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> applyRole(HttpServletRequest request, @RequestBody String body,
			@RequestHeader("userId") String userId) {
		JSONObject json = JSONObject.parseObject(body);
		String reason = json.getString("reason");
		// String roleId=json.getString("roleId");
		String roleCode = json.getString("roleCode");
		if (StringUtils.isEmpty(roleCode)) {
			roleCode = "0001";
		}

		Role role = roleService.getByCode(roleCode);
		if (role == null) {
			return JsonResponse.fail(ErrorCode.ROLE_NOT_EXIST);
		}
		Map<String, Object> data = new HashMap<String, Object>();
		Apply apply = applyService.apply(ApplyAction.AUTH.toString(), userId, "role", role.getUuid(), reason);
		data.put("applyId", apply.getUuid());
		return JsonResponse.successNonNull(data);
	}

	/**
	 * 申请认证状态
	 * 
	 * @param userId
	 *            用户id
	 * @param roleId
	 *            角色id
	 * @return
	 */
	@RequestMapping(value = "applyRoleStatus", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> applyStatus(HttpServletRequest request, @RequestBody String body,
			@RequestHeader("userId") String userId) {
		JSONObject json = JSONObject.parseObject(body);
		String roleCode = json.getString("roleCode");
		if (StringUtils.isEmpty(roleCode)) {
			roleCode = "0001";
		}
		Role role = roleService.getByCode(roleCode);
		if (role == null) {
			return JsonResponse.fail(ErrorCode.ROLE_NOT_EXIST);
		}
		Map<String, Object> data = new HashMap<String, Object>();
		Apply apply = applyService.applyStaus(ApplyAction.AUTH, userId, "role", role.getUuid());
		if (apply != null) {
			data.put("status", apply.getStatus());
		} else {
			data.put("status", -1);
		}
		return JsonResponse.successNonNull(data);
	}

	/**
	 * 批复认证
	 * 
	 * @param applyId
	 *            申请Id
	 * @param action
	 *            动作 0:通过 1:不通过
	 * @return
	 */
	@RequestMapping(value = "replyRole", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> replyRole(HttpServletRequest request, @RequestBody String body) {
		JSONObject json = JSONObject.parseObject(body);
		String applyId = json.getString("applyId");
		String action = json.getString("action");
		Map<String, Object> data = new HashMap<String, Object>();
		int status = "0".equals(action) ? 1 : -1;
		int row = applyService.reply(applyId, status);
		if (row > 0) {
			return JsonResponse.successNonNull(data);
		} else {
			return JsonResponse.fail(ErrorCode.INTERNAL_ERROR);
		}
	}

	/**
	 * 空间
	 */
	@RequestMapping(value = "space", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> space(HttpServletRequest request, @RequestBody String body,
			@RequestHeader("userId") String curUserId) {
		JSONObject json = JSONObject.parseObject(body);
		String userId = json.getString("userId");
		if (userId == null) {
			userId = curUserId;
		}
		// fix under 1.6 , 6
		String appVersionCodeStr = request.getHeader("appVersionCode");
		int appVersionCode = 1;
		if(StringUtils.isNotBlank(appVersionCodeStr)){
			try {
				appVersionCode = Integer.parseInt(appVersionCodeStr);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Integer pageSize = json.getInteger("pageSize");// 每页显示数量
		pageSize = pageSize == null ? 20 : pageSize;
		JSONObject space = userService.space(userId, curUserId, appVersionCode);
		
		//客户端空间请求不需要直播列表
		Integer needContent = json.getInteger("needContent");
		if(needContent==null || needContent==1){
			Pages<Stream> pages = streamService.findStreams(userId, 0, pageSize);
			List<Stream> streamList = pages.getList();
			List<Map<String, Object>> contentList = streamService.getStreams(streamList, true);
			space.put("contentList", contentList);
		}
		
		int totalStreamTime = streamService.getTotalStreamTime(userId);
		space.put("totalStreamTime", totalStreamTime);
		space.put("friendlyTotalStreamTime", DateUtil.formatDuration(totalStreamTime));
		return JsonResponse.successNonNull(space);
	}

	/**
	 * 离线统计
	 */
	@RequestMapping(value = "offlineStatistics", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> offline(HttpServletRequest request, @RequestBody String body) {
		JSONObject json = JSONObject.parseObject(body);
		String action = json.getString("action");
		String object = json.getString("object");
		String userId = json.getString("userId");
		String objectId = json.getString("objectId");
		String ext = json.getString("ext");
		long duration = json.getLong("duration") == null ? 0 : json.getLong("duration");
		offlineService.add(userId, action, object, objectId, duration, ext);
		return JsonResponse.success();
	}

	/**
	 * 绑定商城token
	 * 
	 * @param userId
	 * @param body
	 * @return
	 */
	@RequestMapping(value = "/bindMallUserToken", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> bindMallUserToken(HttpServletRequest request, @RequestBody String body,
			@RequestHeader String userId) {
		JSONObject jsonObject = JSON.parseObject(body);
		String token = jsonObject.getString("token");
		User user = userService.getUserById(userId);
		if (user == null) {
			return JsonResponse.fail(ErrorCode.USER_NOT_EXIST);
		}
		user.setMallUserToken(token);
		userService.update(user);
		return JsonResponse.success();
	}

	@RequestMapping(value = "/start", method = RequestMethod.POST)
	public ResponseEntity<Object> start(HttpServletRequest request, @RequestBody String body,
			@RequestHeader(required = false) String userId, @RequestHeader(required = false) String userToken) {
		JSONObject data = null;
		if(!StringUtils.isEmpty(userId) && !StringUtils.isEmpty(userToken)){
			UserVo userVo = userService.getUserVoByUserId(userId);
			if (userVo != null && userVo.getStatus() == UserStatus.NORMAL.ordinal()) {
				String token = userService.getUserToken(userId);
				if (userToken.equals(token)) {
					//鉴权通过
					data = userService.getStartData(userId, userVo);
					List<Trailer> trailerList = trailerService.getUserTrailer(userId);
					data.put("trailerList", trailerList);
				}
			}
		}
		if(data==null){
			data = new JSONObject();
		}
		//分类
		data.put("creatCategory", categoryService.getCreateCategory());
		data.put("showCategory", categoryService.getShowCategory());
		
		//搜索关键词
		String keywords = configureUtil.getConfig("search_keyword_suggest");
		if(StringUtils.isNotEmpty(keywords)){
			String[] arr = keywords.split(",");		
			data.put("searchKeywordSuggest", Arrays.asList(arr));
		}
		
		//splash
		List<Banner> list = bannerService.selectBannerByLocation(Banner.BannerLocation.Splash.toString());
		if(list!=null && !list.isEmpty()){
			data.put("splash", list.get(0));
		}
		
		//客户端配置
		Map<String,String> configMap = new HashMap<String,String>();
		Iterator<Configure> configs = configureUtil.findByType(Configure.ConfigType.APP.ordinal()).iterator();
		while(configs.hasNext()){
			Configure conf = configs.next();
			configMap.put(conf.getName(), conf.getValue());
		}
		data.put("config", configMap);
		
		// 首页按钮
		data.put("buttons", buttonService.getAllButtonMap());

		//deprecated, to be replaced by buttons
		data.put(Constants.APP_LEFTMODEL_NAME, configureUtil.getConfig(Constants.APP_LEFTMODEL_NAME));
		data.put(Constants.APP_LEFTMODEL_URL, configureUtil.getConfig(Constants.APP_LEFTMODEL_URL));
		data.put(Constants.APP_RIGHTMODEL_NAME, configureUtil.getConfig(Constants.APP_RIGHTMODEL_NAME));
		data.put(Constants.APP_RIGHTMODEL_URL, configureUtil.getConfig(Constants.APP_RIGHTMODEL_URL));

		return JsonResponse.success(data);
	}

	/**
	 * 退出接口
	 * 
	 * @return
	 */
	@RequestMapping(value = "/loginOut", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> loginOut(HttpServletRequest request, @RequestBody String body,
			@RequestHeader String userId) {
		Map<String, Object> data = new HashMap<String, Object>();
		userService.loginOut(userId);
		return JsonResponse.success(data);
	}

	/**
	 * 用户搜索接口
	 * 
	 * @param request
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> search(HttpServletRequest request, @RequestBody JSONObject body,
			@RequestHeader String userId) {
		String keyword = body.getString("keyword");// 关键字
		Integer pageNo = body.getInteger("pageNo");
		pageNo = pageNo == null ? 1 : pageNo;
		Integer pageSize = body.getInteger("pageSize");
		pageSize = pageSize == null ? 10 : pageSize;

		Map<String, Object> data = new HashMap<String, Object>();
		
		SearchHits hits = searchService.keywordSearch(keyword, (pageNo - 1) * pageSize, pageSize);
		List<UserVo> userVos = new ArrayList<UserVo>();
		for (SearchHit hit : hits.getHits()) {
			String uid = hit.getFields().get("user_id").getValue().toString();
			UserVo userVo = userService.getUserVoByUserId(uid);
			// 设置关注关系
			boolean isFollow = userRelationService.exist(userId, userVo.getUuid(), RelationType.FOLLOW.toString());
			userVo.setIsFollow(isFollow ? 1 : 0);
			userVos.add(userVo);
		}
		data.put("list", userVos);
		data.put("totalCount", hits.getTotalHits());
		return JsonResponse.success(data);
	}
}
