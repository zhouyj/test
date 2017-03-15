package com.feedss.user.service;

import java.util.List;
import java.util.Map;

import com.feedss.user.entity.UserRelation;
import com.feedss.user.model.UserRelationVO;

/**
 * 用户关系
 * @author zhangjie
 *
 */
public interface UserRelationService {

	/**
	 * 删除用户关系
	 * @param uuid
	 * @return
	 */
	int del(String userId,String fromUserId,String type);

	
	UserRelation detail(String uuid);

	UserRelation add(String fromUserId, String toUserId, String type,String groupId);

	/**
	 * 是否存在关系记录
	 * @param fromUserId
	 * @param toUserId
	 * @param type
	 * @return true:存在  false:不存在
	 */
	boolean exist(String fromUserId, String toUserId, String type);
	


	/**
	 * 我(他)关注的人
	 * @param curUserId  当前登录的用户
	 * @param userId  curUserId=userId 为我关注的人，反之他关注的人
	 * @param type   类型  关注  朋友
	 * @param cursorId  光标id
	 * @param size  每页个数
	 * @return
	 */
	List<UserRelationVO> userFollow(String curUserId, String userId, String type,
			String cursorId, int size);


	/**
	 * 关注 我(他)的人
	 * @param curUserId  当前登录的用户
	 * @param userId  curUserId=userId 为关注我的人，反之关注他的人
	 * @param type   类型  关注  朋友
	 * @param cursorId  光标id
	 * @param size  每页个数
	 * @return
	 */
	List<UserRelationVO> userFollowBy(String curUserId, String userId,
			String type, String cursorId, int size);


	/**
	 * 关注总数
	 * @param userId
	 * @param type
	 * @return
	 */
	long followCount(String userId, String type);

	/**
	 * 被关注总数
	 * @param userId
	 * @param type
	 * @return
	 */
	long followByCount(String userId, String type);


	UserRelation detail(String toUserId, String fromUserId, String type);

	/**
	 * 人气榜
	 * @param type
	 * @param pageNo
	 * @param pageSize
	 */
	Map<String, Object> xRank(String curUserId, int type, int pageNo,
			int pageSize);


}
