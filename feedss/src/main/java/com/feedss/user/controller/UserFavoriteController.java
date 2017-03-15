package com.feedss.user.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.user.entity.Favorite;
import com.feedss.user.entity.Favorite.FavoriteType;
import com.feedss.user.service.UserFavoriteService;

/**
 * 收藏
 * @author 张杰
 *
 */
@RestController
@RequestMapping("user")
public class UserFavoriteController {

	@Autowired
	UserFavoriteService favoriteService;
	
	
	/**
	 * 添加收藏
	 * @param userId
	 * @param body
	 * @return
	 */
	@RequestMapping(value="favorite",method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> favorite(HttpServletRequest request,@RequestBody String body,@RequestHeader("userId")String userId
			){
		JSONObject json=JSONObject.parseObject(body);
		String object=json.getString("object");
		String objectId=json.getString("objectId");
		String type = json.getString("type");
		if(StringUtils.isEmpty(type)){
			type = FavoriteType.FAVORITE.name();
		}
		String extAttr = json.getString("extAttr");
		favoriteService.add(userId, object, objectId, type, extAttr);
		return JsonResponse.success(new HashMap());
	}
	
	/**
	 * 我的收藏
	 * @param userId
	 * @param body
	 * @return
	 */
	@RequestMapping(value="myFavorites",method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> myFavorites(HttpServletRequest request,@RequestBody String body,@RequestHeader("userId")String userId
			){
		JSONObject json=JSONObject.parseObject(body);
		String cursorId=json.getString("cursorId");
		String object=json.getString("object");
		int pageSize=json.getInteger("pageSize")==null?20:json.getInteger("pageSize");
		String type = json.getString("type");
		String direction = json.getString("direction");//查询方向
		return JsonResponse.response(favoriteService.getFavorites(userId, pageSize, cursorId, object, type, direction));
	}
	
	/**
	 * 删除收藏
	 * @param userId
	 * @param body
	 * @return
	 */
	@RequestMapping(value="unfavorite",method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> unfavorite(HttpServletRequest request,@RequestBody String body,@RequestHeader("userId")String userId){
		JSONObject json=JSONObject.parseObject(body);
		String object=json.getString("object");
		String objectId=json.getString("objectId");
		String type = json.getString("type");
		if(StringUtils.isEmpty(type)){
			type = FavoriteType.FAVORITE.name();
		}
		
		Favorite favorite=favoriteService.detail(userId, object, objectId, type);
		if(favorite==null){
			return JsonResponse.fail(ErrorCode.OBJECT_NOT_EXIST);
		}
		if(!favorite.getUserId().equals(userId)){
			return JsonResponse.fail(ErrorCode.NO_AUTH);
		}
		favoriteService.del(userId, object, objectId, type);
		return JsonResponse.success();
	}
	
	/**
	 * 批量 是否收藏
	 * @param userId
	 * @param body
	 * @return[{object:'user',objectId:'32135sdf'},{object:'user',objectId:'32135sdf'}]
	 */
	@RequestMapping(value="isFavorite",method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> isFavorite(HttpServletRequest request,@RequestBody String body,@RequestHeader("userId")String userId
			){
		Map< String, Object> data=new HashMap();
		if(StringUtils.isEmpty(body)){
			return JsonResponse.success(data);
		}
		JSONArray array=JSONArray.parseArray(body);
		if(array.size()==0){
			return JsonResponse.success(data);
		}
		
		List<Map<String, Object>> result=favoriteService.IsFavorite(userId, array);
		data.put("list", result);
		return JsonResponse.success(data);
	}
	
}
