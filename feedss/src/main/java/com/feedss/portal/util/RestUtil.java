package com.feedss.portal.util;

import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONObject;

/**
 * RestFull API 请求工具
 * @author Looly
 *
 */
public class RestUtil {
	
	public static ThreadLocal<HttpSession> localSession = new ThreadLocal<>();
	
	/**
	 * 获得Session中的用户对象
	 */
	public static JSONObject getUserFromSession(){
		HttpSession session = localSession.get();
		if(session != null){
			JSONObject user = (JSONObject) session.getAttribute(Constants.USER_SESSION);
			return user;
		}
		return null;
	}
	
}
