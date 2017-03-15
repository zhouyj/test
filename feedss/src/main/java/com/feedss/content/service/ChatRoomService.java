package com.feedss.content.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonData;
import com.feedss.base.Row;
import com.feedss.contact.controller.model.GroupParam;
import com.feedss.contact.entity.Group;
import com.feedss.contact.service.GroupService;

@Component
public class ChatRoomService {

	Log logger = LogFactory.getLog(getClass());

	@Autowired
	private GroupService groupService;

	@Async
	public void createChatRoom(String userId, String groupId, String name) {
		GroupParam param = new GroupParam();
		param.setGroupId(groupId);
		param.setType(Group.GroupType.AVChatRoom.name());
		param.setName(name);
		param.setOwnerAccount(userId);

		groupService.createGroup(param);
	}

	/**
	 * 进入聊天室<br>
	 * 
	 * @param userId
	 * @param groupId
	 * @return
	 */
	public int enterChatRoom(String userId, String groupId) {
		JsonData result = groupService.userEnterGroup(groupId, userId);
		if (result == null || result.getCode() != ErrorCode.SUCCESS.getCode()) {
			logger.info("进入聊天室请求失败：groupId=" + groupId + "  userId=" + userId);
			return -1;
		}
		if (result.getCode() == 12006) {
			// group不存在，创建group
			createChatRoom(userId, groupId, "聊天室");
			// 创建成功，再次进入
			result = groupService.userEnterGroup(groupId, userId);
			if (result != null && result.getCode() > 0) {// 用户再次进入失败？？？
				logger.error("进入聊天室请求失败：" + result == null ? "enterResult is null, "
						: result.getCode() + "=" + result.getMsg() + ", userId = " + userId + ", groupId = " + groupId);
				return -1;
			}
			return 0;

		} else {
			if (result.getCode() == 12004) {// 禁止用户进入
				logger.info("进入聊天室请求失败：" + result == null ? "dataEntity is null, "
						: result.getCode() + "=" + result.getMsg() + ", userId = " + userId + ", groupId = " + groupId);
			}
			return result.getCode();
		}
	}

	/**
	 * 退出聊天室<br>
	 * 
	 * @param userId
	 * @param groupId
	 * @return
	 */
	public boolean exitChatRoom(String userId, String groupId) {
		return groupService.userExitGroup(groupId, userId);
	}

	/**
	 * 根据groupId获取观众列表<br>
	 * 
	 * @param groupId
	 * 
	 */
	public List<Map<String, Object>> getAudienceList(String userId, String groupId, String cursorId, Integer pageSize) {

		JsonData result = groupService.getMemberAccounts(groupId, cursorId, pageSize);

		List<Map<String, Object>> reList = new ArrayList<Map<String, Object>>();

		if (result == null || result.getCode() != ErrorCode.SUCCESS.getCode()) {
			logger.info("批量请求失败：" + result == null ? "result is null, "
					: result.getCode() + "=" + result.getMsg() + ", userId = " + userId + ", groupId = " + groupId);
			return reList;
		}

		Row data = (Row) result.getData();
		if (data == null) {
			return reList;
		}
		List<Row> list = (List<Row>) data.get("accountList");
		if (list == null || list.isEmpty()) {
			return reList;
		}
		reList = JSONObject.parseObject(JSONObject.toJSONString(list), new TypeReference<List<Map<String, Object>>>() {
		});
		return reList;
	}
}
