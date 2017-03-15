package com.feedss.user.service;

import java.util.List;

import com.feedss.user.entity.Comment;
import com.feedss.user.model.CommentVo;

/**
 * Created by qinqiang on 2016/8/6.
 */
public interface CommentService {

	public Comment createComment(String userId,String object,String objectId,String title,String content);

	public List<Comment> findComments(String objectId,String cursorId,int pageSize,String direction);

	public List<CommentVo> queryComments(String objectId, String cursorId, int pageSize, String direction);

	public List<Comment> findAll();
}
