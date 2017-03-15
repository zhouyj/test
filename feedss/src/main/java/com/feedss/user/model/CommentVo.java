package com.feedss.user.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.feedss.user.entity.Comment;

import lombok.Data;
import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by qinqiang on 2016/8/7.
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class CommentVo {
	private  String uuid;

	private String userId;

	private String userNickName;

	private String object;

	private String objectId;

	private String title;

	private String content;

	private String created;

	public static CommentVo comment2Vo(Comment comment,String userNickName){
		if(comment==null){
			return null;
		}
		CommentVo commentVo = new CommentVo();
		commentVo.setUuid(comment.getUuid());
		commentVo.setUserId(comment.getUserId());
		commentVo.setUserNickName(userNickName);
		commentVo.setObject(comment.getObject());
		commentVo.setObjectId(comment.getObjectId());
		commentVo.setTitle(comment.getTitle());
		commentVo.setContent(comment.getContent());
		commentVo.setCreated(new DateTime(comment.getCreated()).toString("yyyy-MM-dd HH:mm:ss"));
		return commentVo;
	}

}
