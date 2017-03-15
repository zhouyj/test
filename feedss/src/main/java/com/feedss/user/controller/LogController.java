package com.feedss.user.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.user.entity.Log;
import com.feedss.user.service.LogService;

@RestController
@RequestMapping("log")
public class LogController {
	@Autowired
	LogService logService;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	
	@RequestMapping(value="add",method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> add(HttpServletRequest request,@RequestBody String body){
		JSONObject json = null;
		try{
			json=JSONObject.parseObject(body);
		}catch (Exception e) {
			logger.info("parse json body exception", e);
		}
		
		if(json==null) return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		JSONArray array = json.getJSONArray("list");
		if(array==null || array.isEmpty()){
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}
		List<Log> toAddList = new ArrayList<Log>();
		for(int i=0;i<array.size();i++){
			JSONObject logJson = array.getJSONObject(i);
			logJson.put("deviceExt", logJson.getJSONObject("deviceExt").toJSONString());
			Log log = JSONObject.toJavaObject(logJson, Log.class);
			toAddList.add(log);
		}
		
		Map<String, Object> data=new HashMap<String, Object>();
		logService.add(toAddList);
		return JsonResponse.success(data);
	}
	
}
