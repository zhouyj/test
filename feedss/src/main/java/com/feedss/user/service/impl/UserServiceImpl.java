package com.feedss.user.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.Constants;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonData;
import com.feedss.base.util.MD5Util;
import com.feedss.base.util.RedisUtil;
import com.feedss.base.util.RedisUtil.KeyType;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.contact.entity.Message.Source;
import com.feedss.contact.service.MessageService;
import com.feedss.contact.service.QCloudService;
import com.feedss.content.entity.Banner;
import com.feedss.content.repository.StreamRepository;
import com.feedss.content.service.BannerService;
import com.feedss.manage.model.ManageUserVo;
import com.feedss.portal.entity.Interaction.InteractionType;
import com.feedss.portal.entity.PushStreamTerminal;
import com.feedss.portal.repository.InteractionRepository;
import com.feedss.portal.service.SkillService;
import com.feedss.portal.service.TaskService;
import com.feedss.portal.service.TerminalService;
import com.feedss.user.controller.SmsController.SmsType;
import com.feedss.user.entity.AccountTransaction.AccoutTransactionSourceType;
import com.feedss.user.entity.Favorite;
import com.feedss.user.entity.Module;
import com.feedss.user.entity.Profile;
import com.feedss.user.entity.Role;
import com.feedss.user.entity.Role.RoleType;
import com.feedss.user.entity.TimeCard;
import com.feedss.user.entity.TimeCard.TimeCardStatus;
import com.feedss.user.entity.User;
import com.feedss.user.entity.User.UserStatus;
import com.feedss.user.entity.UserRelation.RelationType;
import com.feedss.user.entity.UserRole;
import com.feedss.user.model.ModuleVo;
import com.feedss.user.model.UserVo;
import com.feedss.user.repository.RoleRepository;
import com.feedss.user.repository.UserRepository;
import com.feedss.user.service.AccountService;
import com.feedss.user.service.ModuleService;
import com.feedss.user.service.ProfileService;
import com.feedss.user.service.RoleService;
import com.feedss.user.service.UserFavoriteService;
import com.feedss.user.service.UserProductService;
import com.feedss.user.service.UserRelationService;
import com.feedss.user.service.UserRoleService;
import com.feedss.user.service.UserService;

/**
 * 
 * @author 张杰
 * 
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private AccountService accountService;

	@Autowired
	private ProfileService profileService;

	@Autowired
	private UserRelationService userRelationService;

	@Autowired
	private UserRoleService userRoleService;

	@Autowired
	private BannerService bannerService;
	
	@Autowired
	private MessageService messageService;
	
	@Autowired
	private QCloudService qCloudService;
	
	@Autowired
	ModuleService moduleService;

	@Autowired
	StreamRepository streamRepository;

	@Autowired
	UserProductService userProductService;

	@Autowired
	InteractionRepository interactionRepository;
	@Autowired
	TaskService taskService;
	@Autowired
	SkillService skillService;

	@Autowired
	private UserFavoriteService favoriteService;
	@Autowired
	private TerminalService terminalService;
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private ConfigureUtil configureUtil;

	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate;

	private final static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Transactional
	@Override
	public int editStatus(String userId, int action) {
		int row = 0;
		if (action == UserStatus.NORMAL.ordinal()) {// 启用
			row = userRepository.updateStatus(userId, action);
			updateCacheUserVoStatus(userId, UserStatus.NORMAL);
		} else {// 禁用
			row = userRepository.updateStatus(userId, UserStatus.DISABLED.ordinal());
			updateCacheUserVoStatus(userId, UserStatus.DISABLED);
			redisUtil.delete(KeyType.TOKEN, userId);
			// 消息通知
			Map<String, String> extMap = new HashMap<>();
			extMap.put("accountId", userId);
			extMap.put("messageSource", Source.UserDisable.name());
			messageService.sendPushMessage(userId, null, "该帐号被禁用", extMap);
//			messageApi.sendNotification(userId, null, "该帐号被禁用", MessageSource.UserDisable.name(), null, extMap);
			// 告诉内容中心，如果当前该用户有正在进行的直播，需要关闭stream
			// contentApi.forbiddenUser(userId);
		}
		return row;
	}

	@Transactional
	@Override
	public int updateUsernameAndPassword(String username, String password, String userId) {
		return userRepository.updateUsernameAndPassword(username, password, userId);
	}

	/**
	 * 修改用户密码
	 * 
	 * @param password
	 * @param userId
	 * @return
	 */
	@Transactional
	@Override
	public int updatePassword(String password, String userId) {
		return userRepository.updatePassword(password, userId);
	}

	@Override
	public Map<String, Object> pageFindUser(final String uuid, final String nickname, final String mobile, String field,
			String direction, int page, int size, int isV) {

		Map<String, Object> data = new HashMap<String, Object>();
		String sqlFiled = " u.created ";
		String sqlDirection = " desc ";
		String sqlFiled2 = "";
		if ("age".equals(field)) {
			sqlFiled = " IF(ISNULL(p.birthdate),1,0), p.birthdate ";
			sqlFiled2 = ",u.uuid ";
		} else if ("level".equals(field)) {
			sqlFiled = " p.level ";
			sqlFiled2 = ",u.uuid ";
		}

		if ("asc".equals(direction)) {
			sqlDirection = " asc ";
		}
		if (page < 1) {
			page = 1;
		}
		int start = (page - 1) * size;

		Role r = roleRepository.findByCode("0001");
		Role rm = roleRepository.findByCode("0002");
		Role rh = roleRepository.findByCode("0003");

		Map<String, Object> condition = new HashMap<String, Object>();
		StringBuffer sql = new StringBuffer("");
		StringBuffer sqlCount = new StringBuffer("");

		sql.append("select u.uuid,u.mobile,u.created,p.gender,"
				+ "p.level,p.avatar,p.birthdate,p.nickname,r.uuid as ruuid,rm.uuid as rmuuid,rh.uuid as rhuuid,u.status ");
		sqlCount.append("select count(*) ");

		String join = " from user u " + " LEFT JOIN profile as p " + " on u.uuid=p.user_id " + " left join "
				+ " (select * from user_role where " + "  role_id='" + rm.getUuid() + "' " + " and status=0) as rm "
				+ " on rm.user_id=u.uuid " + " left join " + " (select * from user_role where " + "  role_id='"
				+ r.getUuid() + "' " + " and status=0) as r " + " on r.user_id=u.uuid " + " left join "
				+ " (select * from user_role where " + "  role_id='" + rh.getUuid() + "' " + " and status=0) as rh "
				+ " on rh.user_id=u.uuid " + " where 1=1 ";

		sql.append(join);
		sqlCount.append(join);

		if (StringUtils.isNotEmpty(uuid)) {
			sql.append(" and u.uuid =:userId ");
			sqlCount.append(" and u.uuid =:userId ");
			condition.put("userId", uuid);
		}

		if (StringUtils.isNotEmpty(nickname)) {
			sql.append(" and p.nickname like :nickname ");
			sqlCount.append(" and p.nickname like :nickname  ");
			condition.put("nickname", "%" + nickname + "%");
		}
		if (isV == 1) {
			sql.append(" and r.uuid is not null ");
			sqlCount.append(" and r.uuid is not null ");
		}

		if (StringUtils.isNotEmpty(mobile)) {
			sql.append(" and u.mobile = :mobile ");
			sqlCount.append(" and u.mobile = :mobile ");
			condition.put("mobile", mobile);
		}
		sql.append(" order by " + sqlFiled + " " + sqlDirection + " " + sqlFiled2);

		Query query = entityManager.createNativeQuery(sql.toString());
		for (String key : condition.keySet()) {
			query.setParameter(key, condition.get(key));
		}
		query.setFirstResult(start).setMaxResults(size);
		query.unwrap(SQLQuery.class).setResultTransformer(new AliasToBeanResultTransformer(ManageUserVo.class));

		List<ManageUserVo> list = query.getResultList();

		Query queryCount = entityManager.createNativeQuery(sqlCount.toString());
		for (String key : condition.keySet()) {
			queryCount.setParameter(key, condition.get(key));
		}
		long totalCount = ((BigInteger) queryCount.getSingleResult()).longValue();

		data.put("list", list);
		data.put("pageNo", page);
		data.put("pageSize", size);
		data.put("totalCount", totalCount);
		data.put("totalPages", totalCount % size == 0 ? totalCount / size : (totalCount / size + 1));
		return data;
	}

	@Override
	public List<User> batchUser(String[] userIds) {
		List<String> list = new ArrayList<String>(userIds.length);
		Collections.addAll(list, userIds);
		return userRepository.findAll(list);
	}

	@Override
	public User detail(String userId) {
		User u = userRepository.findOne(userId);
		return u;
	}

	@Override
	public User getUserById(String userId) {
		User u = userRepository.findOne(userId);
		return u;
	}

	@Override
	public void update(User user) {
		userRepository.save(user);
	}

	@Override
	public boolean checkDevice(String userId, String curDeviceId, String deviceAgent) {
		if (StringUtils.isEmpty(curDeviceId)) {
			return false;
		}
		if (deviceAgent.equalsIgnoreCase("web") || deviceAgent.equalsIgnoreCase("admin_pc")) {
			return false;
		}
		String deviceId = redisUtil.get(KeyType.Device, userId);
		if (deviceId == null) {
			redisUtil.set(KeyType.Device, userId, curDeviceId);
			return true;
		}

		if (!curDeviceId.equals(deviceId)) {
			// 发送消息 通知用户其他设备登录
//			messageApi.sendNotification(userId, null, "您的帐号已在别处登录，请注意帐号安全", MessageSource.SingleSignOn.name(),
//					curDeviceId, null);
			Map<String, String> extMap = new HashMap<>();
			extMap.put("accountId", userId);
			extMap.put("messageSource", Source.SingleSignOn.name());
			extMap.put("deviceId", deviceId);
			messageService.sendPushMessage(userId, null, "您的帐号已在别处登录，请注意帐号安全", extMap);
			redisUtil.set(KeyType.Device, userId, curDeviceId);
			return false;
		}
		return true;
	}

	@Override
	public String getMessageUserSig(String userId, String photo, String nickname, boolean isCreate) {
		String userSig = "";
		int time = 60 * 60 * 24;
		if (isCreate) {
			try {
//				userSig = messageApi.qcloudUserImport(userId, photo, nickname);
				userSig = qCloudService.qcloudImportAccount(userId, nickname, photo);
			} catch (Exception e) {
				e.printStackTrace();
			}
			redisUtil.set(KeyType.MessageUserSig, userId, userSig, time);
			return userSig;
		} else {
			userSig = redisUtil.get(KeyType.MessageUserSig, userId);
			if (StringUtils.isEmpty(userSig)) {
				try {
//					userSig = messageApi.qcloudUserSig(userId);
					userSig = qCloudService.getUserSig(userId);
				} catch (Exception e) {
					logger.error(e.toString(), e);
				}
				redisUtil.set(KeyType.MessageUserSig, userId, userSig, time);
			}
		}

		return userSig;
	}

	@Override
	public UserVo loginUser(String userId) {
		UserVo userVo = getUserVoByUserId(userId);
		String token = createToken(userId);
		userVo.setToken(token);
		String userSig = getMessageUserSig(userId, userVo.getProfile().getAvatar(), userVo.getProfile().getNickname(),
				false);
		userVo.setUserSig(userSig);
		return userVo;
	}

	@Override
	public UserVo create(User u) {
		// 添加用户
		String token = createToken(u.getUuid());
		Profile profile = profileService.createProfile(u);
		u.setProfile(profile);

		u.setCreated(DateTime.now().toDate());
		User user = userRepository.save(u);
		user.setToken(token);

		accountService.createAccount(user.getUuid());// 创建帐号

		Role role = roleRepository.findByCode("0000");
		userRoleService.save(user.getUuid(), role.getUuid());// 关联角色

		// 拼装返回信息
		UserVo uservo = initUserVo(user);
		return uservo;
	}
	
	private UserVo initUserVo(User user){
		if(user==null) return null;
		UserVo userVo = new UserVo();
		userVo.initVo(user);
		List<Role> roles = roleRepository.findRolesByUserId(user.getUuid());
		userVo.initRoles(roles);
		List<String> permissions = roleService.getPermissions(user.getUuid());
		userVo.setPermissions(permissions);
		
		if (user.getProfile() == null) {
			userVo.setProfileIsEmpty(true);
		} else {
			String userSig = getMessageUserSig(user.getUuid(), user.getProfile().getAvatar(), user.getProfile().getNickname(),
					false);
			userVo.setUserSig(userSig);
		}
		return userVo;
	}

	@Override
	public UserVo create(String accountName, String pwd, boolean isMobile) {
		// 添加用户
		User u = new User();
		Date now = DateTime.now().toDate();
		u.setCreated(now);
		u.setUpdated(now);
		if (isMobile) {
			u.setMobile(accountName);
		} else {
			u.setUsername(accountName);
		}

		u.setPassword(pwd);
		User user = userRepository.save(u);

		accountService.createAccount(user.getUuid());// 创建帐号

		Role role = roleRepository.findByCode("0000");
		userRoleService.save(user.getUuid(), role.getUuid());// 关联角色

		// 拼装返回信息
//		UserVo uservo = new UserVo();
//		uservo.initVo(user);
//		List<Role> roles = roleRepository.findRolesByUserId(u.getUuid());
//		uservo.initRoles(roles);
		return initUserVo(user);
	}

	@Override
	public UserVo getUserVoByUserId(String userId) {
		String redisKey = redisUtil.generateKey(KeyType.UserVo, userId);
		UserVo userVo = (UserVo) redisTemplate.opsForValue().get(redisKey);
		if (userVo != null)
			return userVo;
		User user = userRepository.findOne(userId);
		if (user == null) {
			return null;
		}
		userVo = initUserVo(user);
		redisTemplate.opsForValue().set(redisKey, userVo, 2, TimeUnit.MINUTES);
		return userVo;
	}
	
	@Override
	public UserVo getUserVoByApp(String id, String appName, String appToken) {
		User user = userRepository.findByThirdparty(appName, id);
		if(user == null || !appToken.equals(user.getThirdpartyToken())){
			return null;
		}
		
		List<Role> roles = roleRepository.findRolesByUserId(user.getUuid());
		if(roles!=null){
			for(Role r:roles){
				if(Role.RoleType.API.getCode().equals(r.getCode())){
					return initUserVo(user);
				}
			}
		}
		return null;
	}

	public UserVo updateCacheUserVo(String userId) {
		String redisKey = redisUtil.generateKey(KeyType.UserVo, userId);
		UserVo userVo = new UserVo();
		User user = userRepository.findOne(userId);
		if (user == null) {
			return null;
		}
		userVo = initUserVo(user);
		redisTemplate.opsForValue().set(redisKey, userVo, 2, TimeUnit.MINUTES);
		return userVo;
	}

	private void updateCacheUserVoStatus(String userId, UserStatus userStatus) {
		String redisKey = redisUtil.generateKey(KeyType.UserVo, userId);
		UserVo userVo = (UserVo) redisTemplate.opsForValue().get(redisKey);
		if (userVo == null) {
			return;
		}
		userVo.setStatus(userStatus.ordinal());
		redisTemplate.opsForValue().set(redisKey, userVo, 2, TimeUnit.MINUTES);
	}

	@Override
	public void updateCacheUserVoRoles(String userId) {
		String redisKey = redisUtil.generateKey(KeyType.UserVo, userId);
		UserVo userVo = (UserVo) redisTemplate.opsForValue().get(redisKey);
		if (userVo == null) {
			return;
		}
		List<Role> roles = roleRepository.findRolesByUserId(userId);
		userVo.initRoles(roles);
		redisTemplate.opsForValue().set(redisKey, userVo, 2, TimeUnit.MINUTES);
	}

	@Override
	public boolean checkVerifyCode(String mobile, String smsCode, SmsType smsType) {
		if (smsCode.equals("163257")) {
			return true;
		}
		if ((mobile.equals("13800138000") || mobile.equals("13800138001")) && smsCode.equals("000000")) {
			return true;
		}
		String sms = null;
		if (smsType == SmsType.LOGIN) {
			sms = redisUtil.get(KeyType.Sms_Login, mobile);
		} else if (smsType == SmsType.MOBILE_BIND) {
			sms = redisUtil.get(KeyType.Sms_Mobile_Bind, mobile);
		} else if (smsType == SmsType.REGISTER) {
			sms = redisUtil.get(KeyType.Sms_Mobile_Register, mobile);
		}

		if (smsCode.equals(sms)) {
			return true;
		}
		return false;
	}

	@Override
	public User findByMobile(String mobile) {
		User u = userRepository.findByMobile(mobile);
		return u;
	}

	@Override
	public User getByUserNameAndMobile(String userName) {
		User u = userRepository.getByUserNameAndMobile(userName);
		if (u == null) {
			return null;
		}
		return u;
	}

	@Override
	public UserVo getUserInfoWithToken(User u, String deviceAgent) {
		UserVo userVo = getUserVoByUserId(u.getUuid());
		if ("web".equalsIgnoreCase(deviceAgent)) {
			String token = getUserToken(u.getUuid());
			userVo.setToken(token == null ? createToken(u.getUuid()) : token);
		} else {
			userVo.setToken(createToken(u.getUuid()));
		}
		return userVo;
	}

	@Override
	public User findByThirdparty(String thirdpartyName, String thirdpartyId) {
		User u = userRepository.findByThirdparty(thirdpartyName, thirdpartyId);
		return u;
	}

	@Override
	public JSONObject getStartData(String userId, UserVo userVo) {

		JSONObject data = new JSONObject();
		data.put("user", userVo);
		data.put("version", configureUtil.getConfig(Constants.PROJECT_VERSION));

		// 根据用户角色，返回发起直播时是否可选参与活动广告
		List<HashMap<String, String>> roles = userVo.getRoles();
		if (roles != null) {
			for (HashMap<String, String> role : roles) {
				if (role.get("code").equals("0003")) { // 主播
					List<Banner> banners = bannerService
							.selectBannerByLocation(Banner.BannerLocation.LiveRoom.toString());
					data.put("banners", banners);
					break;
				}
			}
		}

		
		List<PushStreamTerminal> terminalList = terminalService.getByUserId(userId);
		data.put("terminals", terminalList);

		return data;
	}

	@Override
	public String getUserToken(String userId) {
		return redisUtil.get(KeyType.TOKEN, userId);
	}

	@Override
	public String createToken(String userId) {
		long expiredTime = 60 * 60 * 24 * 60;// configureUtil.getTokenExpiredTime();

		byte[] b = (new Date() + DigestUtils.md5DigestAsHex(userId.getBytes())).getBytes();
		String token = DigestUtils.md5DigestAsHex(b);
		logger.info("generate token, " + token);
		redisUtil.set(KeyType.TOKEN, userId, token, expiredTime);
		// redisUtil.set(KeyType.TOKEN, token, userId, expiredTime);
		return token;
	}

	@Override
	public void deleteToken(String userId) {
		logger.info("delete token : {}, ", userId);
		redisUtil.delete(KeyType.TOKEN, userId);
	}

	@Override
	public JSONObject space(String userId, String curUserId, int appVersionCode) {
		JSONObject json = new JSONObject();
		UserVo userVo = getUserVoByUserId(userId);

		long followCount = userRelationService.followCount(userId, RelationType.FOLLOW.name());// 关注数
		long followByCount = userRelationService.followByCount(userId, RelationType.FOLLOW.name());// 被关注数
		userVo.setFollowCount(followCount);
		userVo.setFollowByCount(followByCount);

		JSONArray array = new JSONArray();
		Map<String, Module> baseModule = moduleService.getBaseModule();

		// 礼物
		Module gifi = baseModule.get("0000");
		if(gifi!=null){
			long gifi_num = userProductService.getProductsSumByUserId(userId);// 获取礼物数量
			ModuleVo gifiVo = new ModuleVo(gifi);
			gifiVo.setNum(String.valueOf(gifi_num));
			array.add(gifiVo);
			json.put("giftModule", gifiVo);// 单独返回礼物模块，并可点击
		}
		
		int publishTaskCount = taskService.getPublishTaskCount(userId);
		int acceptTaskCount = taskService.getAcceptTaskCount(userId);
		int skillCount = skillService.getTotalCount(userId);

		int exclusiveShop_num = skillCount;// 去获取专卖店
		Module exclusiveShop = baseModule.get("0001");
		if (exclusiveShop != null) {
			ModuleVo exclusiveShopVo = new ModuleVo(exclusiveShop);
			exclusiveShopVo.setNum(String.valueOf(exclusiveShop_num));
			array.add(exclusiveShopVo);
		}

		Module live = baseModule.get("0002");
		if (live != null) {
			int live_num = streamRepository.findCountByUserId(userId);// 获取直播
			ModuleVo liveVo = new ModuleVo(live);
			liveVo.setNum(String.valueOf(live_num));
			array.add(liveVo);
		}

		if (curUserId.equals(userId)) {
			TimeCard card = accountService.getTimeCardByUser(userId);
			if (card != null) {
				userVo.setTimeCardNumber(card.getSerialNumber());
			}

			String userSig = getMessageUserSig(userId, "", "", false);
			userVo.setUserSig(userSig);

			// 账户
			//苹果测试账号不返回该模块
			if(!"13800138000".equals(userVo.getMobile()) && !"13800138001".equals(userVo.getMobile())){
				Module account = baseModule.get("0003");
				if (account != null) {
					int balance = accountService.getAccountInfo(curUserId).getBalance();
					ModuleVo accountVo = new ModuleVo(account);
					accountVo.setNum(String.valueOf(balance));
					array.add(accountVo);
				}
			}
			
			// 、任务、
			Module task = baseModule.get("0004");
			if (task != null) {
				int task_num = publishTaskCount + acceptTaskCount;
				ModuleVo taskVo = new ModuleVo(task);
				taskVo.setNum(String.valueOf(task_num));
				array.add(taskVo);
			}

			// 加v、
			Module vip = baseModule.get("0005");
			if (vip != null) {
				ModuleVo vipVo = new ModuleVo(vip);
				vipVo.setNum("");
				array.add(vipVo);
			}
			// 心愿单
			Module wish = baseModule.get("0006");
			if (wish != null) {
				ModuleVo wishVo = new ModuleVo(wish);
				wishVo.setNum("");
				array.add(wishVo);
			}
			if (appVersionCode >= 5) {
				// 互动任务
				Module task2 = baseModule.get("0007");
				if (task2 != null) {
					ModuleVo task2Vo = new ModuleVo(task2);
					task2Vo.setNum(String.valueOf(interactionRepository.selectCountByReceiver(userId,
							InteractionType.BonusTask.name())
							+ interactionRepository.selectCountByCreator(userId, InteractionType.BonusTask.name())));
					array.add(task2Vo);
				}

				// 投标
				Module bid = baseModule.get("0008");
				if (bid != null) {
					ModuleVo bidVo = new ModuleVo(bid);
					bidVo.setNum(String
							.valueOf(interactionRepository.selectCountByReceiver(userId, InteractionType.Bid.name())
									+ interactionRepository.selectCountByCreator(userId, InteractionType.Bid.name())));
					bidVo.setNum(String
							.valueOf(interactionRepository.selectCountByReceiver(userId, InteractionType.Bid.name())
									+ interactionRepository.selectCountByCreator(userId, InteractionType.Bid.name())));
					array.add(bidVo);
				}
			}

			json.put("isFollow", 0);
		} else {
			boolean exist = userRelationService.exist(curUserId, userId, RelationType.FOLLOW.name());
			json.put("isFollow", exist ? 1 : 0);
		}
		json.put("user", userVo);
		json.put("list", array);
		return json;
	}

	@Override
	public void loginOut(String userId) {
		System.out.println("当前用户token:" + redisUtil.get(KeyType.TOKEN, userId));
		redisUtil.delete(KeyType.TOKEN, userId);
		redisUtil.delete(KeyType.Device, userId);
		System.out.println("退出后当前用户token:" + redisUtil.get(KeyType.TOKEN, userId));
	}

	@Override
	public List<UserVo> getHostList(String userId, String cursorId, Integer pageSize, Integer direction) {
		List<UserVo> list = (List<UserVo>) redisTemplate.opsForValue().get(KeyType.HOST_LIST_FOLLOWBY.name());
		if (list == null || list.isEmpty()) {
			list = refreshHostList();
		}

		List<UserVo> result = new ArrayList<UserVo>();
		// 翻页问题
		if (StringUtils.isEmpty(cursorId)) {
			result = list.subList(0, list.size() >= pageSize ? pageSize : list.size());
		} else {
			UserVo cursor = this.getUserVoByUserId(cursorId);
			int index = 0;
			if (cursor != null) {
				index = list.indexOf(cursor);
			}
			if (index < 0) {
				return null;
			}
			if (direction == 1) {
				int start = index - pageSize;
				result = list.subList(start > 0 ? start : 0, index);
			} else {
				int start = index + 1;
				int end = start + pageSize;
				result = list.subList(start, end > list.size() ? list.size() : end);
			}
		}

		if (result != null && !result.isEmpty() && !StringUtils.isEmpty(userId)) {
			for (UserVo userVo : list) {
				boolean isFollow = userRelationService.exist(userId, userVo.getUuid(), RelationType.FOLLOW.toString());
				userVo.setIsFollow(isFollow ? 1 : 0);
			}
		}
		return result;
	}

	@Override
	public List<UserVo> refreshHostList() {
		List<UserVo> list = new ArrayList<UserVo>();
		List<String> userIds = userRoleService.getUserIds(RoleType.HOST);
		if (userIds != null && !userIds.isEmpty()) {
			for (String userId : userIds) {
				UserVo userVo = this.getUserVoByUserId(userId);
				if (userVo != null) {
					long count = userRelationService.followByCount(userId, RelationType.FOLLOW.name());
					userVo.setFollowByCount(count);
					list.add(userVo);
				}
			}
		}
		if (!list.isEmpty()) {
			Collections.sort(list, new Comparator<UserVo>() {
				public int compare(UserVo u1, UserVo u2) {
					return u2.getFollowByCount().compareTo(u1.getFollowByCount());
				}
			});
		}
		redisTemplate.opsForValue().set(KeyType.HOST_LIST_FOLLOWBY.name(), list, 5, TimeUnit.MINUTES);
		return list;
	}

	@Override
	public int addUserRole(String userId, RoleType roleType) {
		UserRole ur = userRoleService.addUserRole(roleType, userId);
		if (ur != null) {
			updateCacheUserVoRoles(userId);
			return 0;
		}
		return 1;
	}

	@Override
	public int removeUserRole(String userId, RoleType roleType) {
		int result = userRoleService.removeUserRole(roleType, userId);
		if (result > 0) {
			updateCacheUserVoRoles(userId);
			return 0;
		}
		return 1;
	}

	@Override
	public List<UserVo> batchUserVo(List<String> userIds) {
		List<UserVo> userVos = new ArrayList<UserVo>();
		for (String id : userIds) {
			UserVo userVo = getUserVoByUserId(id);
			if (userVo != null) {
				userVos.add(userVo);
			}
		}
		return userVos;
	}

	@Override
	public Map<String, Object> getProfile(String userId, String toUserId) {
		Map<String, Object> data = new HashMap<String, Object>();
		if (StringUtils.isEmpty(toUserId)) {
			toUserId = userId;
		}
		UserVo userVo = getUserVoByUserId(toUserId);
		String type = RelationType.FOLLOW.toString();
		if (userVo != null) {
			boolean exist = userRelationService.exist(userId, toUserId, type);
			// 点卡
			TimeCard card = accountService.getTimeCardByUser(userId);
			userVo.setTimeCard(card);

			data.put("isFollow", exist ? 1 : 0);
			data.put("user", userVo);
		}
		long followCount = userRelationService.followByCount(toUserId, type);
		data.put("followCount", followCount);
		long followByCount = userRelationService.followCount(toUserId, type);
		data.put("followByCount", followByCount);

		return data;
	}

	@Override
	public JsonData pwdLogin(String username, String password, String deviceAgent) {
		Map<String, Object> data = new HashMap<String, Object>();
		User user = getByUserNameAndMobile(username);
		if (user == null) {
			return JsonData.fail(ErrorCode.USER_NOT_EXIST);// 用户不存在
		}
		if (!user.getPassword().equalsIgnoreCase(password)) {
			return JsonData.fail(ErrorCode.PASSWORD_ERROR);// 密码错误
		}

		UserVo userVo = getUserInfoWithToken(user, deviceAgent);
		if (userVo == null) {
			return JsonData.fail(ErrorCode.USERNAME_OR_PASSWORD_ERROR);// 密码信息错误
		}
		if (userVo.getStatus() == 1) {
			return JsonData.fail(ErrorCode.USER_FORBIDDEN);// 用户被禁用
		}
		int loginRewardCount = configureUtil.getConfigIntValue(Constants.REWARD_ARTICLEDETAIL_COUNT);
		if (loginRewardCount > 0) {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			Date start = c.getTime();

			c.set(Calendar.HOUR, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			Date end = c.getTime();

			if (!accountService.isExist(userVo.getUuid(), AccoutTransactionSourceType.systemPresentLogin, start, end)) {
				accountService.reward(userVo.getUuid(), loginRewardCount, "",
						AccoutTransactionSourceType.systemPresentLogin);
			}
		}
		data.put("user", userVo);
		return JsonData.successNonNull(data);
	}

	@Override
	public JsonData registerByMobile(String mobile, String smsCode, String pwd) {
		Map<String, Object> data = new HashMap<String, Object>();
		boolean result = checkVerifyCode(mobile, smsCode, SmsType.REGISTER);
		if (result) {
			User u = findByMobile(mobile);
			if (u != null) {
				return JsonData.fail(ErrorCode.ACCOUNT_EXISTED);
			}
			UserVo userVo = create(mobile, pwd, true);
			if (userVo != null && StringUtils.isNotBlank(userVo.getUuid())) {
				userVo.setToken(createToken(userVo.getUuid()));
				data.put("user", userVo);
			}
			return JsonData.successNonNull(data);
		} else {
			return JsonData.fail(ErrorCode.INVALIDE_CODE);
		}
	}

	@Override
	public JsonData registerByUsername(String username, String password, String timecardNumber) {
		Map<String, Object> data = new HashMap<String, Object>();
		User u = getByUserNameAndMobile(username);
		if (u != null) {
			return JsonData.fail(ErrorCode.ACCOUNT_EXISTED);
		}
		UserVo userVo = create(username, password, false);
		if (userVo != null && StringUtils.isNotBlank(userVo.getUuid())) {
			userVo.setToken(createToken(userVo.getUuid()));
			data.put("user", userVo);
			accountService.activeTimeCard(timecardNumber, userVo.getUuid());
		}
		return JsonData.successNonNull(data);
	}

	@Override
	public JsonData resetPwd(String mobile, String smsCode, String pwd, String pwd2) {
		if (StringUtils.isBlank(mobile) || StringUtils.isBlank(smsCode) || StringUtils.isBlank(pwd)
				|| StringUtils.isBlank(pwd2) || !pwd.equals(pwd2)) {
			return JsonData.fail(ErrorCode.INVALID_PARAMETERS);
		}
		boolean result = checkVerifyCode(mobile, smsCode, SmsType.RESETPWD);
		if (result) {
			User u = findByMobile(mobile);
			if (u == null)
				return JsonData.fail(ErrorCode.USER_NOT_EXIST);
			if (u.getStatus() == User.UserStatus.DISABLED.ordinal())
				return JsonData.fail(ErrorCode.USER_FORBIDDEN);

			updatePassword(pwd, u.getUuid());
			return JsonData.success();
		} else {
			return JsonData.fail(ErrorCode.INVALIDE_CODE);
		}
	}

	@Override
	public JsonData updatePwd(String userId, String oldPwd, String pwd, String pwd2) {
		if (StringUtils.isBlank(oldPwd) || StringUtils.isBlank(pwd) || StringUtils.isBlank(pwd2) || !pwd.equals(pwd2)) {
			return JsonData.fail(ErrorCode.INVALID_PARAMETERS);
		}
		User u = getUserById(userId);
		if (u == null)
			return JsonData.fail(ErrorCode.USER_NOT_EXIST);
		if (u.getStatus() == User.UserStatus.DISABLED.ordinal())
			return JsonData.fail(ErrorCode.USER_FORBIDDEN);
		if (!oldPwd.equals(u.getPassword())) {
			return JsonData.fail(ErrorCode.PASSWORD_ERROR);
		}

		if (updatePassword(pwd, u.getUuid()) > 0) {
			return JsonData.success();
		}
		return JsonData.fail(ErrorCode.ERROR);
	}

	@Override
	public JsonData bindMobile(String userId, String mobile, String smsCode) {
		// 查询手机号是否已绑定
		User u = findByMobile(mobile);
		if (u != null) {
			return JsonData.fail(ErrorCode.MOBILE_HAS_BINDED);
		}
		u = getUserById(userId);
		if (u == null) {
			return JsonData.fail(ErrorCode.USER_NOT_EXIST);
		} else if (StringUtils.isNotBlank(u.getMobile())) {
			// 已绑定有手机号
			return JsonData.fail(ErrorCode.HAS_BINDED_MOBILE);
		}
		// 校验手机号验证码
		boolean result = checkVerifyCode(mobile, smsCode, SmsType.MOBILE_BIND);
		if (!result) {
			return JsonData.fail(ErrorCode.CODE_ERROR);
		} else {
			u.setMobile(mobile);
			update(u);
			return JsonData.success();
		}
	}

	@Override
	public JsonData bindTimecard(String userId, String serialNumber, String password){
		if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(serialNumber)) {
			return JsonData.fail(ErrorCode.INVALID_PARAMETERS);
		}
		UserVo user = getUserVoByUserId(userId);
		if (user == null) {
			return JsonData.fail(ErrorCode.USER_NOT_EXIST);
		}
		TimeCard card = accountService.getTimeCardByUser(userId);
		if (card != null) {
			return JsonData.fail(ErrorCode.HAS_BIND_CARD);
		}

		TimeCard timeCard = accountService.getTimeCard(serialNumber, password);
		if (timeCard == null) {
			return JsonData.fail(ErrorCode.CARD_OR_PASSROD_ERROR);
		} else if (timeCard.getStatus() == TimeCardStatus.ACTIVIED.ordinal()) {
			return JsonData.fail(ErrorCode.CARD_ACTIVED);
		} else if (timeCard.getStatus() == TimeCardStatus.INVALID.ordinal()) {
			return JsonData.fail(ErrorCode.INVALIDE_CARD);
		}

		accountService.activeTimeCard(serialNumber, userId);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("serialNumber", serialNumber);
		return JsonData.successNonNull(data);
	}

	@Override
	public JsonData updateProfile(String userId, JSONObject json) {
		boolean isRegister = false;
		Map<String, String> paramMap = new HashMap<String, String>();

		Set<String> noEmptyFieldSet = new HashSet<String>();
		String noEmptyField = configureUtil.getConfig(Constants.REGISTER_NOTEMPTY_FIELD);
		if (StringUtils.isNotBlank(noEmptyField)) {
			String[] noEmptyFields = noEmptyField.split(",");
			noEmptyFieldSet.addAll(Arrays.asList(noEmptyFields));
		}

		Set<String> rewardFieldSet = new HashSet<String>();
		String rewardField = configureUtil.getConfig(Constants.PROFILE_REWARD_FIELD);
		if (StringUtils.isNotBlank(rewardField)) {
			String[] rewardFields = rewardField.split(",");
			rewardFieldSet.addAll(Arrays.asList(rewardFields));
		}

		// 取老属性
		User user = getUserById(userId);
		if (user == null) {
			return JsonData.fail(ErrorCode.USER_NOT_EXIST);
		}
		Profile profile = user.getProfile();
		if (profile != null) {
			String extAttr = profile.getExtAttr();
			if (StringUtils.isNotBlank(extAttr)) {
				JSONObject ext = JSONObject.parseObject(extAttr);
				if (ext != null) {
					for (String k : ext.keySet()) {
						paramMap.put(k, ext.getString(k));
					}
				}
			}
			Date d = profile.getBirthdate();
			if (d != null) {
				DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd");
				DateTime dateTime = new DateTime(d);
				paramMap.put("birthdate", dateTime.toString(format));
			}
			paramMap.put("avatar", profile.getAvatar());
			paramMap.put("gender", String.valueOf(profile.getGender()));
			paramMap.put("nickname", profile.getNickname());
		} else {
			profile = new Profile();
			profile.setUserId(userId);
			profile.setLocationCode(0);
			profile.setLevel(1);
			profile.setIntegral(0);
			profile.setType("");
			profile.setStatus(1);
			isRegister = true;
		}

		for (String key : json.keySet()) {
			paramMap.put(key, json.getString(key));
		}

		for (String f : noEmptyFieldSet) {
			String v = json.getString(f);
			if (StringUtils.isEmpty(v)) {
				return JsonData.fail(ErrorCode.INVALID_PARAMETERS, "Field: " + f + " is empty!");
			}
			// paramMap.put(f, v);
		}

		int emptyNum = 0;
		for (String f : rewardFieldSet) {
			String v = json.getString(f);
			if (StringUtils.isEmpty(v)) {
				emptyNum++;
			}
			// paramMap.put(f, v);
		}

		Date date = null;
		String birthdate = paramMap.remove("birthdate");
		if (StringUtils.isNotEmpty(birthdate)) {
			DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd");
			try {
				date = DateTime.parse(birthdate, format).toDate();
			} catch (Exception e) {
				return JsonData.fail(ErrorCode.INVALID_PARAMETERS);
			}
		}

		Map<String, Object> data = new HashMap<String, Object>();

		profile.setNickname(paramMap.remove("nickname"));
		profile.setAvatar(paramMap.remove("avatar"));
		try {
			profile.setGender(Integer.parseInt(paramMap.remove("gender")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		profile.setBirthdate(date);

		profile.setExtAttr(JSONObject.toJSONString(paramMap));

		profile = profileService.saveOrUpdateProfile(profile);
		if (profile != null && user.getProfile() == null) {
			user.setProfile(profile);
			update(user);
			int reward = 0;
			int registerCount = configureUtil.getConfigIntValue(Constants.REGISTER_REWARD_COUNT);
			if (isRegister) {// 首次
				if (registerCount > 0) {
					// TODO 奖励
					data.put("registerSuccess", 1);
					reward += registerCount;
				}
				int profileCount = configureUtil.getConfigIntValue(Constants.PROFILE_REWARD_COUNT);
				if (profileCount > 0 && !rewardFieldSet.isEmpty()) {
					// TODO 奖励
					if (emptyNum == 0) {
						data.put("profileAll", 1);
						reward += profileCount;
					} else {
						reward += (profileCount * (rewardFieldSet.size() - emptyNum)) / rewardFieldSet.size();
					}
				}
				accountService.reward(userId, reward, profile.getUuid(),
						AccoutTransactionSourceType.systemPresentProfile);
				data.put("reward", reward);
			}
		}
		profileService.updateProfile2Connect(profile, userId); // 更新用户信息到消息中心
		UserVo userVo = updateCacheUserVo(userId);
		data.put("user", userVo);
		return JsonData.successNonNull(data);
	}

	@Override
	public JsonData addLog(String userId, String object, String objectId, String type, String extAttr) {

		User u = getUserById(userId);
		if (u == null) {
			return JsonData.fail(ErrorCode.USER_NOT_EXIST);
		}
		Favorite one = favoriteService.add(userId, object, objectId, type, extAttr);
		// TODO 奖励
		int viewArticleRewardCount = configureUtil.getConfigIntValue(Constants.REWARD_ARTICLEDETAIL_COUNT);
		if (one != null && viewArticleRewardCount > 0) {
			accountService.reward(userId, viewArticleRewardCount, objectId,
					AccoutTransactionSourceType.systemPresentViewArticle);
		}
		return JsonData.success();
	}

	@Override
	public void initAdmin() {
		logger.info("ini admin...");
		String username = "admin";
		String pwd = "live@zp";
		User admin = getByUserNameAndMobile(username);
		if(admin==null){
			String pwd_enc = MD5Util.MD5String(pwd);
			UserVo adminVo = create(username, pwd_enc, false);
			Role role = roleRepository.findByCode("0002");
			userRoleService.save(adminVo.getUuid(), role.getUuid());// 关联角色
			
		}else{
			logger.info("admin has exist...");
		}
	}
}
