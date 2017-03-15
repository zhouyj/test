package com.feedss.user.service;

import com.feedss.user.entity.Like;
import com.feedss.user.entity.Like.ListType;

/**
 * 点赞服务
 * 
 * @author tangjun
 *
 */
public interface LikeService {

	/**
	 * 是否存在
	 * @param userId
	 * @param object
	 * @param objectId
	 * @return
	 */
	boolean isExist(String userId, String object, String objectId,ListType type);

	Like add(ListType type, String userId, String object, String objectId,
			int count);

	/**
	 * 获取点赞总数
	 * @param object
	 * @param objectId
	 * @return
	 */
	long getLikeCount(String object, String objectId);
	
	
	

}
