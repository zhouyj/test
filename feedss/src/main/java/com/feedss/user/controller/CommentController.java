package com.feedss.user.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.user.entity.Comment;
import com.feedss.user.model.CommentVo;
import com.feedss.user.service.CommentService;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by qinqiang on 2016/8/6.
 */
@RestController
@RequestMapping("user/comment")
public class CommentController {

	@Autowired
	private CommentService commentService;

	@RequestMapping("create")
	public ResponseEntity<Object> createComment(HttpServletRequest request, @RequestBody String body,@RequestHeader String userId){
		JSONObject jsonOBody = JSON.parseObject(body);
		String object = jsonOBody.getString("object");//对象名称
		String objectId = jsonOBody.getString("objectId");//对象id
		String title = jsonOBody.getString("title");//评论标题
		String content = jsonOBody.getString("content");//评论内容
		if(StringUtils.isBlank(object) || StringUtils.isBlank(objectId) ||StringUtils.isBlank(content)){
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}
		commentService.createComment(userId,object,objectId,title,content);
		return JsonResponse.success(new HashMap<>());
	}
	@RequestMapping("list")
	public ResponseEntity<Object>  queryComment(HttpServletRequest request,@RequestBody String body,@RequestHeader String userId){
		HashMap<String,Object> data = new HashMap<String,Object>();
		JSONObject jsonOBody = JSON.parseObject(body);
		String object = jsonOBody.getString("object");//对象名称
		String objectId = jsonOBody.getString("objectId");//对象id
		String cursorId = jsonOBody.getString("cursorId");//光标id
		int pageSize = jsonOBody.getIntValue("pageSize"); //分页行数
		String direction = jsonOBody.getString("direction");//查询方向
		List<CommentVo> commentVos = commentService.queryComments(objectId,cursorId,pageSize,direction);
		data.put("list",commentVos);
		return JsonResponse.success(data);
	}

}
