package com.feedss.user.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
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
import com.feedss.base.util.CommonUtil;
import com.feedss.content.service.RoomService;
import com.feedss.user.entity.User;
import com.feedss.user.entity.UserRelation;
import com.feedss.user.entity.UserRelation.FollowType;
import com.feedss.user.entity.UserRelation.RelationType;
import com.feedss.user.model.UserRelationVO;
import com.feedss.user.service.UserRelationService;
import com.feedss.user.service.UserService;

/**
 * 用户关系
 * @author 张杰
 *
 */
@RestController
@RequestMapping("user/relation")
public class UserRelationController {

	@Autowired
	UserRelationService userRelationService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	RoomService roomService;
	
	Logger logger=Logger.getLogger(getClass());
	
	/**
	 * 是否关注
	 * @param request
	 * @param userId
	 * @param body
	 * @return
	 */
	@RequestMapping(value="isFollow",method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> isFollow(HttpServletRequest request,
			@RequestBody String body,
			@RequestHeader("userId") String userId){
		JSONObject json=JSONObject.parseObject(body);
		String fromUserId=userId;
		String toUserId=json.getString("userId");
		String type=RelationType.FOLLOW.toString();
		boolean existUr=userRelationService.exist(fromUserId, toUserId, type);
		Map<String, Object> map=new HashMap<String,Object>();
		if(existUr){
			map.put("isFollow", 1);
		}else{
			map.put("isFollow", 0);
		}
		return JsonResponse.success(map);
	}
	
	/**
	 * 添加关注
	 * @return
	 */
	@RequestMapping(value="follow",method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> follow(HttpServletRequest request,
			@RequestBody String body,@RequestHeader("userId") String userId){
		JSONObject json=JSONObject.parseObject(body);
		String fromUserId=userId;
		String toUserId=json.getString("userId");
		String groupId=json.getString("groupId");
		String type=RelationType.FOLLOW.toString();
		
		if(userId.equals(toUserId)){
			return JsonResponse.fail(ErrorCode.CANT_FOLLOW_SELF);
		}
		
		Map<String, Object> map=new HashMap<String,Object>();
		UserRelation ur = userRelationService.add(fromUserId, toUserId, type, groupId);
		if(ur!=null){
			roomService.updateAudienceRelation(fromUserId, toUserId, 1);
		}
		map.put("uuid", ur.getUuid());
		return JsonResponse.success(map);
	}
	
	/**
	 * 取消关注
	 * @return
	 */
	@RequestMapping(value="unFollow",method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> unFollow(HttpServletRequest request,
			@RequestBody String body,@RequestHeader("userId") String userId){
		JSONObject json=JSONObject.parseObject(body);
		String toUserId=json.getString("userId");
		if(toUserId==null){
			JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}
		
		userRelationService.del(toUserId, userId, RelationType.FOLLOW.toString());
		roomService.updateAudienceRelation(toUserId, userId, FollowType.UNFOLLOW.ordinal());

		return JsonResponse.success(new HashMap());
	}
	
	/**
	 * 用户关注的用户
	 * @return
	 */
	@RequestMapping(value="userFollow",method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> userFollow(HttpServletRequest request,
			@RequestBody(required=false) String body,@RequestHeader("userId") String userId){
		if(body==null){
			body="{}";
		}
		JSONObject json=JSONObject.parseObject(body);
		String uid=json.getString("userId");//得到列表的用户id;
		String cursorId=json.getString("cursorId");
		int size=json.getInteger("pageSize")==null?20:json.getInteger("pageSize");
		if(uid==null){
			uid=userId;
		}
		Map<String, Object> data=new HashMap<String, Object>();
		
		long count=userRelationService.followCount(uid, RelationType.FOLLOW.name());
		List<UserRelationVO> list=userRelationService.userFollow(userId, uid, 
				RelationType.FOLLOW.toString(), cursorId, size);
		data.put("list", list);
		data.put("totalCount", count);
		return JsonResponse.success(data);
	}
	
	/**
	 * 关注用户的用户
	 * @return
	 */
	@RequestMapping(value="followUser",method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> followUser(HttpServletRequest request,
			@RequestBody(required=false) String body,@RequestHeader("userId") String userId){
		if(body==null){
			body="{}";
		}				
		JSONObject json=JSONObject.parseObject(body);
		String uid=json.getString("userId");//得到列表的用户id;
		String cursorId=json.getString("cursorId");
		int size=json.getInteger("pageSize")==null?20:json.getInteger("pageSize");
		if(uid==null){
			uid=userId;
		}
		Map<String, Object> data=new HashMap<String, Object>();
		
		long count=userRelationService.followByCount(uid, RelationType.FOLLOW.name());
		List<UserRelationVO> list=userRelationService.userFollowBy(userId, uid, 
				RelationType.FOLLOW.toString(), cursorId, size);
		data.put("list", list);
		data.put("totalCount", count);
		return JsonResponse.success(data);
	}
	
	/**
	 * 拉入黑名单
	 * @return
	 */
	@RequestMapping(value="black",method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> addToBlack(HttpServletRequest request,
			@RequestBody String body,@RequestHeader("userId") String userId){
		JSONObject json=JSONObject.parseObject(body);
		String fromUserId=userId;
		String toUserId=json.getString("userId");
		String type=RelationType.BLACK.toString();
		
		if(userId.equals(toUserId)){
			return JsonResponse.fail(ErrorCode.CANT_FOLLOW_SELF);
		}
		
		User toUser=userService.detail(toUserId);
		if(toUser==null){
			return JsonResponse.fail(ErrorCode.USER_NOT_EXIST);
		}
		Map<String, Object> map=new HashMap<String,Object>();
		
		Object o=CommonUtil.getKey(userId+":"+toUserId);
		synchronized (o) {
			try{
				//TODO 是否做消息那边拉黑处理
				//暂时是客户端直接使用腾讯云sdk做了
				
				//如果a关注b，自动取消关注
				if(userRelationService.exist(fromUserId, toUserId, RelationType.FOLLOW.name())){
					if(userRelationService.del(toUserId, fromUserId, RelationType.FOLLOW.name())<=0){
						logger.error("delete black error, from = " + fromUserId + ", to = " + toUserId);
					}else{
						roomService.updateAudienceRelation(fromUserId, toUserId, FollowType.UNFOLLOW.ordinal());
					}
				}
				
				boolean existUr=userRelationService.exist(fromUserId, toUserId, type);
				if(existUr){
					return JsonResponse.fail(ErrorCode.HAS_FOLLOW);
				}
				
				UserRelation ur=userRelationService.add(fromUserId, toUserId, type, null);
				map.put("uuid", ur.getUuid());
			}catch(Exception e){
				logger.error(e.toString(),e);
				return JsonResponse.fail(ErrorCode.ERROR);
			}
		}
		return JsonResponse.success(map);
	}

	/**
	 * 取消拉黑
	 * @return
	 */
	@RequestMapping(value="unBlack",method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> unBlack(HttpServletRequest request,
			@RequestBody String body,@RequestHeader("userId") String userId){
		JSONObject json=JSONObject.parseObject(body);
		String toUserId=json.getString("userId");
		if(toUserId==null){
			JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}
		User toUser=userService.detail(toUserId);
		if(toUser==null){
			return JsonResponse.fail(ErrorCode.USER_NOT_EXIST);
		}
		
		String type=RelationType.BLACK.toString();
		//TODO 是否做消息那边拉黑处理
		//暂时是客户端直接使用腾讯云sdk做了
		
		userRelationService.del(toUserId, userId, type);
		return JsonResponse.success(new HashMap());
	}
	
	
	
}
