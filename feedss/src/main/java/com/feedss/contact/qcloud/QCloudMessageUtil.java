package com.feedss.contact.qcloud;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.Row;
import com.feedss.base.util.HttpUtil;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.contact.controller.model.Notification;
import com.feedss.contact.entity.AccountUsersig;
import com.feedss.contact.entity.GroupMessage;
import com.feedss.contact.entity.Message;
import com.feedss.contact.qcloud.domain.ActionResponse;
import com.feedss.contact.qcloud.domain.BatchSendMessageRequestBody;
import com.feedss.contact.qcloud.domain.GroupMsgRequestBody;
import com.feedss.contact.qcloud.domain.ImportUserRequestBody;
import com.feedss.contact.qcloud.domain.PushMessageRequestBody;
import com.feedss.contact.qcloud.domain.QErrorAccount;
import com.feedss.contact.qcloud.domain.QMessageTransfer;
import com.feedss.contact.repository.AccountUsersigRepository;
import com.google.gson.Gson;

/**
 * 云消息 1、申请账号 2、配置app 3、配置一个账号管理员，用于调用接口：admin（需要在云管理平台配置）
 * 4、服务侧config数据库中配置三个账号：（1）3中配置的管理员账号；（2）不产生聊天会话的推送者账号；（3）用于发送系统消息的账号；
 * 5、app属性初始化； 6、账号初始导入 7、账号签名定期更新
 * 
 * @author zhouyujuan
 *
 */
@Component
@EnableScheduling
public class QCloudMessageUtil {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ConfigureUtil configureUtil;

	@Autowired
	private QMessageTransfer qMessageTransfer;

	@Autowired
	private AccountUsersigRepository userSigRepository;
	
	@Autowired
	private GenerateTLSSignatureUtil generateTLSSignatureUtil;

	public final static String QCLOUD_SDKAPPId_KEY = "qcloud_sdkappid";
	public final static String QCLOUD_PRIVSTR_KEY = "qcloud_privstr";//私钥
	public final static String QCLOUD_PUBSTR_KEY = "qcloud_pubstr";//公钥
	public final static String QCLOUD_ADMIN_IDENTIFIER_KEY = "qcloud_admin_identifier";// 账号管理员
	public final static String QCLOUD_NOSESSION_IDENTIFIER_KEY = "qcloud_nosession_identifier";// 不产生会话的账号
	public final static String QCLOUD_SYSTEMMESSAGE_IDENTIFIER_KEY = "qcloud_systemmessage_identifier";// 系统消息的发送账号
	public final static String QCLOUD_SYSTEMMESSAGE_NICKNAME_KEY = "qcloud_systemmessage_nickname";// 系统消息的发送账号
	public final static String QCLOUD_SYSTEMMESSAGE_FACEURL_KEY = "qcloud_systemmessage_faceurl";// 系统消息账号的头像

	private final static String ACCOUNT_IMPORT_URL = "https://console.tim.qq.com/v4/im_open_login_svc/account_import";

	private final static String BATCH_SEND_MSG = "https://console.tim.qq.com/v4/openim/batchsendmsg";// 单聊消息－批量发单聊消息
	private final static String IM_PUSH_MSG = "https://console.tim.qq.com/v4/openim/im_push";// 消息推送，按attr推送
	private final static String SEND_GROUP_MSG = "https://console.tim.qq.com/v4/group_open_http_svc/send_group_msg";//群组消息

	private static final String USER_TAG_URL = "https://console.tim.qq.com/v4/openim/im_add_tag";

	private static final String USER_TAG_DEL_URL = "https://console.tim.qq.com/v4/openim/im_remove_tag";

	private static final String USER_ATTR_SET_URL = "https://console.tim.qq.com/v4/openim/im_set_attr"; // 设置用户属性

	private static final String USER_ATTR_DEL_URL = "https://console.tim.qq.com/v4/openim/im_remove_attr";// 删除用户属性

	private static final String USERATTR_SET = "https://console.tim.qq.com/v4/openim/im_set_attr_name"; // 设置应用属性名称

	private static final String ADD_GROUP_URL = "https://console.tim.qq.com/v4/group_open_http_svc/create_group";

	private static final String ENTER_GROUP_URL = "https://console.tim.qq.com/v4/group_open_http_svc/add_group_member";

	private static final String EXIT_GROUP_URL = "https://console.tim.qq.com/v4/group_open_http_svc/delete_group_member";

    private static final String FORBID_GROUP_URL = "https://console.tim.qq.com/v4/group_open_http_svc/forbid_send_msg";

	public static final String ATTR_GROUP = "group";
	public static final String ATTR_ID = "id";

	private static Map<String, String> AppAttrNames = new HashMap<String, String>();
	static {
		AppAttrNames.put("0", ATTR_GROUP);
		AppAttrNames.put("1", ATTR_ID);
	}

	private long sdkAppId = 0l;

	private String adminIdentifier;
	private String userSig;

	private String noSessionIdentifier;
	private String systemMessageIdentifier;
	private String systemMessageNickname;
	private String systemMessageFaceUrl;

	public String getSystemMessageIdentifier() {
		return this.systemMessageIdentifier;
	}

	public QCloudMessageUtil() {
	}

	@PostConstruct
	private void init() {
		logger.info("init QCloudMessageUtil...");
		String sdkAppIdStr = configureUtil.getConfig(QCLOUD_SDKAPPId_KEY);
		if (!StringUtils.isEmpty(sdkAppIdStr)) {
			try {
				sdkAppId = Long.parseLong(sdkAppIdStr);
			} catch (NumberFormatException e) {
				logger.error("parse sdkappid error", e);
			}
			if (sdkAppId != 0) {
				adminIdentifier = configureUtil.getConfig(QCLOUD_ADMIN_IDENTIFIER_KEY);
				if (StringUtils.isEmpty(adminIdentifier)) {
					logger.error("QCloudMessageUtil failure, have not get valid admin identifier, " + adminIdentifier);
					return;
				}
				AccountUsersig adminUserSig = userSigRepository.findByAccountId(adminIdentifier);
				if (adminUserSig == null) {
					adminUserSig = new AccountUsersig();
					adminUserSig.setAccountId(adminIdentifier);
				} 
				if (StringUtils.isEmpty(adminUserSig.getUserSig())) {
					userSig = generateTLSSignatureUtil.getUserSig(adminIdentifier);
					if (StringUtils.isEmpty(userSig)) {
						logger.error("QCloudMessageUtil failure, get userSig error.");
						return;
					}
					adminUserSig.setUserSig(userSig);
					userSigRepository.save(adminUserSig);
				} else {
					userSig = adminUserSig.getUserSig();
				}
				// 设置app全局属性
				setAppAttrs();

				noSessionIdentifier = configureUtil.getConfig(QCLOUD_NOSESSION_IDENTIFIER_KEY);
				systemMessageIdentifier = configureUtil.getConfig(QCLOUD_SYSTEMMESSAGE_IDENTIFIER_KEY);
				systemMessageNickname = configureUtil.getConfig(QCLOUD_SYSTEMMESSAGE_NICKNAME_KEY);
				systemMessageFaceUrl = configureUtil.getConfig(QCLOUD_SYSTEMMESSAGE_FACEURL_KEY);

				if (!StringUtils.isEmpty(noSessionIdentifier)) {
					importAccount(noSessionIdentifier, "", "");
				}
				if (!StringUtils.isEmpty(systemMessageIdentifier)) {
					importAccount(systemMessageIdentifier, systemMessageNickname, systemMessageFaceUrl);
				}
			} else {
				logger.error("QCloudMessageUtil failure, have not get valid sdkAppId, ");
				return;
			}
		}
	}

	/**
	 * 导入账号
	 * 
	 * @param accountId
	 * @return
	 */
	public boolean importAccount(String accountId, String nickname, String faceUrl) {
		HttpHeaders headers = new HttpHeaders();
		headers.clear();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.add("Accept", MediaType.APPLICATION_JSON_UTF8.toString());
		headers.add("Accept-Charset", "utf-8");
		logger.info("Header:" + headers.toString());
		
		RestTemplate restTemplate = new RestTemplate();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setConnectTimeout(300000); //5 min
		requestFactory.setConnectionRequestTimeout(300000); //5 min
		requestFactory.setReadTimeout(300000); //5 min
		restTemplate.setRequestFactory(requestFactory);
	
		Object request = new ImportUserRequestBody(accountId, nickname, faceUrl);
		Gson gson = new Gson();
		String requestStr = gson.toJson(request);
		String requestUrl = getRequestUrl(ACCOUNT_IMPORT_URL);

		/**
		 * 使用HttpUtil即正确返回，初步判断请求的RestTemplate请求发的不对
		**/
		String resp = "";
		try{
			resp = HttpUtil.postUrlWithBody(requestUrl, requestStr, "utf-8");
		}catch(Exception e){
			e.printStackTrace();
		}
		logger.info("import user to qcloud, req = " + requestStr + ", resp = " + resp);
		ActionResponse respObj = gson.fromJson(resp, ActionResponse.class);
		String ActionStatus = respObj.getActionStatus();
		if (!ActionStatus.equals("OK")) {
			logger.info("import account error, " + resp);
			return false;
		}
		return true;
	}
	
	// 设置用户属性
	public boolean addAttrToUser(String key, String value, String memberAccount) {
		String url = getRequestUrl(USER_ATTR_SET_URL);
		List<Row> userAttrs = new ArrayList<>();

		Row userAttr = new Row();
		userAttr.put("To_Account", memberAccount);
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put(key, value);
		userAttr.put("Attrs", attrs);

		userAttrs.add(userAttr);

		Row params = new Row();
		params.put("UserAttrs", userAttrs);
		Row rlt = HttpUtil.doPostBody(url, params, "utf-8", Row.class);
		return rlt != null && rlt.getInt("ErrorCode", -1) == 0;
	}

	// 删除用户属性
	@Async
	public void removeUserAttr(String key, String memberAccount) {
		String url = getRequestUrl(USER_ATTR_DEL_URL);
		List<Row> userAttrs = new ArrayList<>();
		Row userAttr = new Row();
		userAttr.put("To_Account", memberAccount);
		userAttr.put("Attrs", new String[] { key });
		userAttrs.add(userAttr);

		Row params = new Row();
		params.put("UserAttrs", userAttrs);
		Row rlt = HttpUtil.doPostBody(url, params, "utf-8", Row.class);
		boolean flag = rlt != null && rlt.getInt("ErrorCode", -1) == 0;
		if(!flag){
			logger.error("removeUserAttr fail, " + JSONObject.toJSONString(userAttr));
		}
	}


	/**
	 * 设置应用属性名称
	 * 
	 * @return
	 */
	@Async
	public void setAppAttrs() {
		String url = getRequestUrl(USERATTR_SET);
		Row params = new Row();
		params.put("AttrNames", AppAttrNames);
		Row rlt = HttpUtil.doPostBody(url, params, "utf-8", Row.class);
		boolean flag = rlt != null && rlt.getInt("ErrorCode", -1) == 0;
		if(!flag){
			logger.error("setAppAttrs fail, " + JSONObject.toJSONString(params));
		}
	}

	// 给用户打标签
	@Async
	public void addTagToUser(String tagName, String memberAccount) {
		// 添加用户标签
		String url = getRequestUrl(USER_TAG_URL);
		List<Row> tags = new ArrayList<>();
		Row tag = new Row();
		tag.put("To_Account", memberAccount);
		tag.put("Tags", new String[] { tagName });
		tags.add(tag);
		Row params = new Row();
		params.put("UserTags", tags);
		Row rlt = HttpUtil.doPostBody(url, params, "utf-8", Row.class);
		boolean flag = rlt != null && rlt.getInt("ErrorCode", -1) == 0;
		if(!flag){
			logger.error("addTagToUser fail, " + JSONObject.toJSONString(params));
		}
	}

	// 删除用户标签
	@Async
	public void removeUserTag(String tagName, String memberAccount) {
		String url = getRequestUrl(USER_TAG_DEL_URL);
		List<Row> tags = new ArrayList<>();
		Row tag = new Row();
		tag.put("To_Account", memberAccount);
		tag.put("Tags", new String[] { tagName });

		tags.add(tag);
		Row params = new Row();
		params.put("UserTags", tags);
		Row rlt = HttpUtil.doPostBody(url, params, "utf-8", Row.class);
		boolean flag = rlt != null && rlt.getInt("ErrorCode", -1) == 0;
		if(!flag){
			logger.error("removeUserTag fail, " + JSONObject.toJSONString(params));
		}
	}

	/**
	 * 直播间消息, 群消息
	 * 
	 * @param message
	 */
	@Async
	public void sendGroupMessage(GroupMessage message) {
		if (message == null){
			logger.error("message is null");
			return;
		}
		GroupMsgRequestBody qMsg = new GroupMsgRequestBody();
		qMessageTransfer.transfer(message, qMsg);
		qMsg.setFrom_Account(noSessionIdentifier);// 客户端拦截，不产生会话
		
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		Gson gson = new Gson();
		String requestStr = gson.toJson(qMsg);
		String requestUrl = getRequestUrl(SEND_GROUP_MSG);
		String resp = restTemplate.postForObject(requestUrl, requestStr, String.class);
		logger.info("send group message, " + requestStr + " and resp, " + resp);
		ActionResponse<String> respObj = gson.fromJson(resp, ActionResponse.class);
		String ActionStatus = respObj.getActionStatus();
		if (!ActionStatus.equals("OK")) {
			logger.error("send group message error, " + resp);
		} 
	}

	/**
	 * 发点对点消息,单聊消息－批量发单聊消息 or push to all
	 * 
	 * @param message
	 */
	@Async
	public void sendSystemMessage(Message message) {
		if (message == null)
			return;
		if(message.getToAccount()==null || message.getToAccount().length<=0){
			// push to all
			PushMessageRequestBody qMsg = new PushMessageRequestBody();
			qMessageTransfer.transfer(message, qMsg);
			pushMessage(qMsg);
		}else{
			//batch send
			BatchSendMessageRequestBody qMsg = new BatchSendMessageRequestBody();
			qMessageTransfer.transfer(message, qMsg);
			batchSendMessage(qMsg);
		}
	}

	/**
	 * 发通知，单聊消息－批量发单聊消息 or push to all
	 * 
	 * @param notification
	 * @return
	 */
	@Async
	public void push(Notification notification) {
		if (notification == null) {
			return;
		}
		if(notification.getToAccount()==null || notification.getToAccount().length<=0){
			PushMessageRequestBody qMsg = new PushMessageRequestBody();
			qMessageTransfer.transfer(notification, qMsg);
			qMsg.setFrom_Account(noSessionIdentifier);// 客户端拦截，不产生会话
			pushMessage(qMsg);
		}else{
			BatchSendMessageRequestBody qMsg = new BatchSendMessageRequestBody();
			qMessageTransfer.transfer(notification, qMsg);
			qMsg.setFrom_Account(noSessionIdentifier);// 客户端拦截，不产生会话
			batchSendMessage(qMsg);
		}
	}
	
	/**
	 * 推送
	 * @param qMsg
	 * @return
	 */
	@Async
	public void pushMessage(PushMessageRequestBody qMsg) {
		if (qMsg == null)
			return;
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		Gson gson = new Gson();
		String requestStr = gson.toJson(qMsg);
		String requestUrl = getRequestUrl(IM_PUSH_MSG);
		String resp = restTemplate.postForObject(requestUrl, requestStr, String.class);
		logger.info("push message, " + requestStr + " and resp, " + resp);
		// TODO
		ActionResponse<String> respObj = gson.fromJson(resp, ActionResponse.class);
		String ActionStatus = respObj.getActionStatus();
		if (!ActionStatus.equals("OK")) {
			logger.error("push message error, " + resp);
		}
	}

	/**
	 * 批量下发 消息 p2p
	 * 
	 * @param qMsg
	 * @return
	 */
	@Async
	public void batchSendMessage(BatchSendMessageRequestBody qMsg) {
		// SEND p2p MSG
		RestTemplate restTemplate = new RestTemplate();
		Gson gson = new Gson();
		String requestStr = gson.toJson(qMsg);
		String requestUrl = getRequestUrl(BATCH_SEND_MSG);		
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		String resp = restTemplate.postForObject(requestUrl, requestStr, String.class);
		logger.info("send p2p message: req = " + requestStr + ", resp = " + resp);
		ActionResponse<List<QErrorAccount>> respObj = gson.fromJson(resp, ActionResponse.class);
		String ActionStatus = respObj.getActionStatus();
		if (!ActionStatus.equals("OK")) {
			logger.info("send p2p message error, " + resp);
			return;
		} else {
			List<QErrorAccount> errorList = respObj.getT();
			if (errorList != null && !errorList.isEmpty()) {
				for (QErrorAccount error : errorList) {
					logger.info("send fail, error = " + gson.toJson(error));
				}
			}
			return;
		}
	}

	/**
	 * 检查用户的sig是否过期，一天一查，发现还有10天过期，重新生成一个
	 */
	@Scheduled(cron = "0 0 0 * * ?") // 每天0点执行一次
	public void refreshAdminSeg() {
		logger.info("refresh admin user qcloud usersig..." + new Date());
		Iterable<AccountUsersig> list = userSigRepository.findAll();
		if (list == null) {
			logger.info("there is no account user sig need to refresh...");
			return;
		}

		for (AccountUsersig userSig : list) {
			if (generateTLSSignatureUtil.needRefreshUserSig(userSig.getAccountId(), userSig.getUserSig())) {
				logger.info("update user sig, accountId = " + userSig.getAccountId());
				String sig = generateTLSSignatureUtil.getUserSig(userSig.getAccountId());
				userSig.setUserSig(sig);
				userSigRepository.save(userSig);
			}
		}
	}

	// 创建群
	@Async
	public void addGroup(String groupId, String groupName, String type, String masterUserId) {
		String url = getRequestUrl(ADD_GROUP_URL);
		Row userAttr = new Row();
		userAttr.put("Owner_Account", masterUserId);
		userAttr.put("Type", type);
		userAttr.put("GroupId", groupId);
		userAttr.put("Name", groupName);

		Row rlt = HttpUtil.doPostBody(url, userAttr, "utf-8", Row.class);
		boolean flag = rlt != null && rlt.getInt("ErrorCode", -1) == 0;
		if(!flag){
			logger.error("addGroup fail, " + JSONObject.toJSONString(userAttr));
		}
	}

	// 进入群
	@Async
	public void enterGroup(String groupId, String userId) {
		String url = getRequestUrl(ENTER_GROUP_URL);
		List<Row> users = new ArrayList<>();

		Row one = new Row();
		one.put("Member_Account", userId);
		users.add(one);

		Row params = new Row();
		params.put("GroupId", groupId);
		params.put("MemberList", users);

		Row rlt = HttpUtil.doPostBody(url, params, "utf-8", Row.class);
		boolean flag = rlt != null && rlt.getInt("ErrorCode", -1) == 0;
		if(!flag){
			logger.error("enterGroup fail, " + JSONObject.toJSONString(params));
		}
	}

	// 退出群
	@Async
	public void exitGroup(String groupId, String userId) {
		String url = getRequestUrl(EXIT_GROUP_URL);
		List<String> users = new ArrayList<>();
		users.add(userId);

		Row params = new Row();
		params.put("GroupId", groupId);
		params.put("MemberToDel_Account", users);

		Row rlt = HttpUtil.doPostBody(url, params, "utf-8", Row.class);
		boolean flag = rlt != null && rlt.getInt("ErrorCode", -1) == 0;
		if(!flag){
			logger.error("exitGroup fail, " + JSONObject.toJSONString(params));
		}
	}

    //禁言
	@Async
    public void forbidGroupUser(String groupId, String userId, long shutUpTime) {
        String url = getRequestUrl(FORBID_GROUP_URL);
        List<String> users = new ArrayList<>();
        users.add(userId);

        Row params = new Row();
        params.put("GroupId", groupId);
        params.put("Members_Account", users);
        params.put("ShutUpTime", shutUpTime);

        Row rlt = HttpUtil.doPostBody(url, params, "utf-8", Row.class);
        boolean flag = rlt != null && rlt.getInt("ErrorCode", -1) == 0;
        if(!flag){
			logger.error("forbidGroupUser fail, " + JSONObject.toJSONString(params));
		}
    }

	public long getSdkAppId() {
		return sdkAppId;
	}

	public String getRequestUrl(String apiUrl) {
		int randomInt = getRandom();
		String requestUrl = new String(apiUrl + "?sdkappid=" + sdkAppId + "&identifier=" + adminIdentifier + "&usersig="
				+ userSig + "&contenttype=json&random=" + randomInt);
		logger.info("========requestUrl = "+ requestUrl);
		return requestUrl;
	}

	public static int getRandom() {
		Random random = new Random();
		int randomInt = random.nextInt(999999999 - 100000000 + 1) + 100000000;
		return randomInt;
	}

}
