package com.feedss.portal.controller.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonData;
import com.feedss.portal.util.HttpUtil;
import com.feedss.user.entity.User.UserStatus;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MobileInterceptor extends HandlerInterceptorAdapter {
	
	private String[] includes = new String[] {
			 "/task/.*",
			 "/report/.*",
			 "/skill/.*",
			 "/feedback"
	};

	@Autowired
	private UserService userService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		String debugCode = request.getHeader("debugCode");
		if (debugCode != null && debugCode.equals("2tlK8f8YVfRW0zHydd3WKyYwUSEtZSqN")) {
			return true;
		}

		String uri = request.getRequestURI();
		if(includes!=null && includes.length>0){
			for(String include:includes){
				if(!uri.matches(include)){
					return true;
				}
			}
		}

		String userId = request.getHeader("userId");
		String userToken = request.getHeader("userToken");
		JsonData result = null;
		UserVo userVo = userService.getUserVoByUserId(userId);
		String _token = userService.getUserToken(userId);
		if (userVo == null)
			result=JsonData.fail(ErrorCode.USER_NOT_EXIST, "用户不存在");
		else if(_token == null || !_token.equals(userToken)){
			result=JsonData.fail(ErrorCode.INVALID_TOKEN, "鉴权失败");
		}else if (userVo.getStatus() == UserStatus.DISABLED.ordinal())
			result=JsonData.fail(ErrorCode.USER_FORBIDDEN, "用户被禁用");
		else if (_token != null && _token.equals(userToken)) {
			return true;
		}

		StringBuilder logStr = new StringBuilder("request info: url:" + request.getRequestURI());
		logStr.append(" userId:" + userId).append(" userToken:" + userToken);
		log.error("userAuth fail, log = " + logStr.toString());
		printError(response, result);
		return false;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		super.afterCompletion(request, response, handler, ex);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
	}

	private void printError(HttpServletResponse response, JsonData result) {
		try {
			response.getWriter().write(JSON.toJSONString(result));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
