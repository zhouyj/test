package com.feedss.user.model;

import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;

import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.feedss.user.entity.UserRelation;

/**
 * 
 * @author 张杰
 *
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class UserRelationVO {

	UserVo fromUser;
	
	UserVo toUser;
	
	int follow;//关注  0:未关注  1:关注
	
	int followBy;//被关注  0:未被关注  1:被关注
	
	String uuid;
	
	String fromUserId;
	
	String toUserId;
	
	String created;//
	
	public UserRelationVO(UserRelation ur){
		this.fromUser=new UserVo();
		fromUser.initVo(ur.getFromUser());
		this.toUser=new UserVo();
		System.out.println(ur.getToUserId());
		System.out.println(ur.getToUser());
		toUser.initVo(ur.getToUser());
		this.uuid=ur.getUuid();
		this.fromUserId=ur.getFromUserId();
		this.toUserId=ur.getToUserId();
		DateTime dateTime = new DateTime(ur.getCreated());
		this.created=dateTime.toString("yyyy-MM-dd HH:mm:ss");
	}
	
}
