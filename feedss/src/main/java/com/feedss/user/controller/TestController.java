package com.feedss.user.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.JsonResponse;
import com.feedss.base.util.RedisUtil;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.UserService;
import com.feedss.user.service.impl.UserServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/**
 * Created by qinqiang on 2016/8/1.
 */
@RestController
@RequestMapping("hhh")
public class TestController {

	@Autowired
	RedisUtil redisUtil;
	
	@Autowired
	UserService userService;
	
	@RequestMapping(value="hhh",method= RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> test(HttpServletRequest request,@RequestBody JSONObject json){
		System.out.println(json);
		return JsonResponse.success(json);
	}
	
	@RequestMapping(value="token",method= RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> test1(HttpServletRequest request,@RequestBody JSONObject json){
		String userId=json.getString("userId");
		String token =userService.getUserToken(userId);
		Map<String, Object> map=new HashMap<String, Object>();
		map.put("token", token);
		return JsonResponse.success(map);
	}

}
