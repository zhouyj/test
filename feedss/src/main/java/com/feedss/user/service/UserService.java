package com.feedss.user.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.JsonData;
import com.feedss.user.controller.SmsController.SmsType;
import com.feedss.user.entity.Role.RoleType;
import com.feedss.user.entity.User;
import com.feedss.user.model.UserVo;

/**
 * 用户服务
 * 
 * @author tangjun
 *
 */
public interface UserService {

	/**
	 * 初始化管理员
	 */
	public void initAdmin();
	
	public boolean checkVerifyCode(String mobile, String smsCode, SmsType smsType);

	public User findByMobile(String mobile);

	public UserVo create(User u);

	public UserVo create(String accountName, String pwd, boolean isMobile);

	public void deleteToken(String userId);

	public String createToken(String userId);

	public String getUserToken(String userId);

	public List<User> batchUser(String[] userIds);

	public User detail(String userId);

	public User getUserById(String userId);

	public void update(User user);

	public UserVo getUserVoByUserId(String userId);
	
	public List<UserVo> batchUserVo(List<String> userIds);

	public UserVo updateCacheUserVo(String userId);

	public void updateCacheUserVoRoles(String userId);

	/**
	 * 登录用户信息
	 * 
	 * @param userId
	 * @return UserVo
	 */
	UserVo loginUser(String userId);

	/**
	 * 空间
	 * 
	 * @param userId
	 * @param curUserId
	 * @return
	 */
	JSONObject space(String userId, String token, int appVersionCode);

	User findByThirdparty(String thirdpartyName, String thirdpartyId);

	/**
	 * 验证设备 且不是同一设备发送消息:其他设备登录
	 * 
	 * @param userId
	 * @param curDeviceId
	 * @return true:同一设备 ，false:不是同一设备
	 */
	boolean checkDevice(String userId, String curDeviceId, String deviceAgent);

	JSONObject getStartData(String userId, UserVo userVo);

	UserVo getUserInfoWithToken(User u, String deviceAgent);

	/**
	 * 查询用户 分页
	 * 
	 * @param uuid
	 * @param nickname
	 * @param mobile
	 * @param filed
	 * @param direction
	 * @param page
	 * @param size
	 * @return
	 */
	Map<String, Object> pageFindUser(String uuid, String nickname, String mobile, String field, String direction,
			int page, int size, int isV);

	/**
	 * 获取消息userSig
	 * 
	 * @param userId
	 * @param isCreate
	 *            是否注册 true:注册
	 * @return
	 */

	String getMessageUserSig(String userId, String photo, String nickname, boolean isCreate);

	/**
	 * 编辑用户状态
	 * 
	 * @param userId
	 * @param action
	 * @return
	 */
	public int editStatus(String userId, int action);

	public void loginOut(String userId);

	/**
	 * 修改用户账户名及密码
	 * 
	 * @param username
	 * @param password
	 * @param userId
	 * @return
	 */
	public int updateUsernameAndPassword(String username, String password, String userId);

	/**
	 * 修改用户密码
	 * 
	 * @param password
	 * @param userId
	 * @return
	 */
	public int updatePassword(String password, String userId);

	public User getByUserNameAndMobile(String userName);
	/**
	 * 获取主播列表
	 * 根据userId 获取关注关系
	 * @return
	 */
	public List<UserVo> getHostList(String userId, String cursorId,Integer pageSize,Integer direction);
	
	public List<UserVo> refreshHostList();
	
	public int addUserRole(String userId, RoleType roleType);
	
	public int removeUserRole(String userId, RoleType roleType);
	
	public Map<String, Object> getProfile(String userId, String toUserId);

	public JsonData pwdLogin(String username, String password, String deviceAgent);
	
	public JsonData registerByMobile(String mobile, String smsCode, String pwd);
	
	public JsonData registerByUsername(String username, String password, String timecardNumber);
	
	public JsonData resetPwd(String mobile, String smsCode, String pwd, String pwd2);
	
	public JsonData updatePwd(String userId, String oldPwd, String pwd, String pwd2);

	public JsonData bindMobile(String userId, String mobile, String smsCode);

	public JsonData bindTimecard(String userId, String serialNumber, String password);
	
	public JsonData updateProfile(String userId, JSONObject json);
	
	public JsonData addLog(String userId, String object, String objectId, String type, String extAttr);

	UserVo getUserVoByApp(String id, String appName, String appToken);
}
