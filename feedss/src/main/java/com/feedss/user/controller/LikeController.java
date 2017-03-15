package com.feedss.user.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.user.entity.Like.ListType;
import com.feedss.user.service.LikeService;

/**
 * 点赞
 * @author zhangjie
 *
 */
@RestController
@RequestMapping("user")
public class LikeController {

	@Autowired
	LikeService likeService;
	
	/**
	 * 点赞 无限制
	 * @param userId
	 * @param body
	 * @return
	 */
	@RequestMapping(value="like",method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> like(HttpServletRequest request,@RequestBody String body,@RequestHeader("userId")String userId){
		JSONObject json=JSONObject.parseObject(body);
		String object=json.getString("object");
		String objectId=json.getString("objectId");
		Integer count =json.getInteger("count");
		if(count==null){
			count=1;
		}
		Map<String, Object> data=new HashMap<String, Object>();
		likeService.add(ListType.Unlimited, userId, object, objectId, count);
		long like_count=likeService.getLikeCount(object, objectId);
		data.put("count", like_count);
		return JsonResponse.success(data);
	}
	
	/**
	 * 点赞 限制
	 * @param userId
	 * @param body
	 * @return
	 */
	@RequestMapping(value="limitLike",method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> limitLike(HttpServletRequest request,@RequestBody String body,@RequestHeader("userId")String userId){
		JSONObject json=JSONObject.parseObject(body);
		String object=json.getString("object");
		String objectId=json.getString("objectId");
		Integer count =1;
		boolean isExist=likeService.isExist(userId, object, objectId,ListType.Limit);
		if(isExist){
			return JsonResponse.fail(ErrorCode.HAS_LIKED);
		}
		
		Map<String, Object> data=new HashMap<String, Object>();
		likeService.add(ListType.Limit, userId, object, objectId, count);
		long like_count=likeService.getLikeCount(object, objectId);
		data.put("count", like_count);
		return JsonResponse.success(data);
	}
	
	/**
	 * 获取点赞数量
	 * @param userId
	 * @param body
	 * @return
	 */
	@RequestMapping(value="likeCount",method=RequestMethod.POST)
	@ResponseBody
	public  ResponseEntity<Object> likeCount(HttpServletRequest request,@RequestBody String body,@RequestHeader("userId")String userId){
		JSONObject json=JSONObject.parseObject(body);
		String object=json.getString("object");
		String objectId=json.getString("objectId");
		
		Map<String, Object> data=new HashMap<String, Object>();
		long like_count=likeService.getLikeCount(object, objectId);
		data.put("count", like_count);
		return JsonResponse.success(data);
	}
	
	
}
