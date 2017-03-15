package com.feedss.user.service;

import java.util.Map;

import com.feedss.user.entity.Apply;
import com.feedss.user.entity.Apply.ApplyAction;

/**
 * 申请认证
 * @author 张杰
 *
 */
public interface ApplyService {

	/**
	 * 申请认证
	 * 将添加需要认证信息
	 * @param action
	 * @param userId
	 * @param object
	 * @param objectId
	 * @param reason
	 * @return
	 */
	Apply apply(String action, String userId, String object, String objectId,
			String reason);

	/**
	 * 
	 * @param applyId
	 * @param status 1:通过  -1:不通过
	 * @return 受影响行
	 */
	int reply(String applyId, int status);

	/**
	 * 获取审批
	 * @param action
	 * @param userId
	 * @param object
	 * @param objectId
	 * @return
	 */
	Apply applyStaus(ApplyAction action, String userId, String object,
			String objectId);

	/**
	 * 申请大V列表
	 * @param uuid
	 * @param nickname
	 * @param mobile
	 * @param filed
	 * @param direction
	 * @param page
	 * @param size
	 * @return
	 */
	Map<String, Object> getApplyVList(String uuid, String nickname,
			String mobile, String field, String direction, int page, int size);
	
}
