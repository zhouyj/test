package com.feedss.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feedss.user.entity.Like;
import com.feedss.user.entity.Like.ListType;
import com.feedss.user.repository.LikeRepository;
import com.feedss.user.service.LikeService;

/**
 * 点赞
 * @author zhangjie
 *
 */
@Service
public class LikeServiceImpl implements LikeService {

	@Autowired
	LikeRepository likeRepository;
	
	
	@Override
	public Like add(ListType type,String userId,String object,String objectId,int count){
		Like like=new Like();
		like.setCount(count);
		like.setUserId(userId);
		like.setObject(object);
		like.setObjectId(objectId);
		like.setType(type.name());
		return likeRepository.save(like);
	}
	
	@Override
	public long getLikeCount(String object ,String objectId){
		return likeRepository.getCount(object, objectId);
	}
	
	@Override
	public boolean isExist(String userId,String object,String objectId,ListType type){
		int isExist=likeRepository.exist(object, objectId, userId,type.name());
		return isExist>0?true:false;
	}
	
	
	
}
