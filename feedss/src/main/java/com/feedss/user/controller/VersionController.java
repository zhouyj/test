package com.feedss.user.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.JsonResponse;
import com.feedss.user.entity.Version;
import com.feedss.user.service.UserManageVersionService;

/**
 * 版本管理
 * @author 张杰
 *
 */
@RestController
@RequestMapping("user/version")
public class VersionController {
	
	@Autowired
	UserManageVersionService manageVersionService;

	/**
	 * 某渠道最新版本
	 * @param request
	 * @param body
	 * @return
	 */
	@RequestMapping("newVersion")
	public ResponseEntity<Object> newVersion(HttpServletRequest request,@RequestBody String body){
		JSONObject json=JSONObject.parseObject(body);
		String channel=json.getString("channel");
		if(StringUtils.isEmpty(channel)){
			channel="feedss";
		}
		Map<String, Object> data=new HashMap<String, Object>();
		Version mv=manageVersionService.getNewVersion(channel);
		if(mv==null){
			mv=manageVersionService.getNewVersion("feedss");
		}
		if(mv!=null){
			data.put("version", mv.getVersion());
			data.put("version_code",  mv.getVersion_code());
			data.put("device_type",  mv.getDevice_type());
			data.put("update_info",  mv.getUpdate_info());
			data.put("update_type",  mv.getUpdate_type());
			data.put("down_url",  mv.getDown_url());
			data.put("channel",  mv.getChannel());
			return JsonResponse.success(data);
		}else{
			return JsonResponse.success(new JSONObject());
		}
		
	}
		
	
}
