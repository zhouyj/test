package com.feedss.user.controller.intercept;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.util.RedisUtil;
import com.feedss.user.entity.Role;
import com.feedss.user.entity.UserRole;
import com.feedss.user.service.RoleService;
import com.feedss.user.service.UserRoleService;
/**
 * 管理后台登录拦截
 * @author 张杰
 *
 */
@Service
public class ManageLoginInterceptor extends HandlerInterceptorAdapter {
	//TODO 开发阶段先全放开
	private final String[] excludes = new String[]{};
	
	private final String[] inners=new String[]{};
			
	@Autowired
	RedisUtil redisUtil;
	
	Logger logger=Logger.getLogger(ManageLoginInterceptor.class);
	
	@Autowired
	UserRoleService userRoleService;
	
	@Autowired
	RoleService roleService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String uri = request.getRequestURI();

		//地址过滤
		if (excludes.length > 0) {
			for (String exclude : excludes) {
				if (uri.matches(exclude)) {
					return true;
				}
			}
		}
		
		if (checkUserLogin(request)) {
			return true;
		}
		
		String userId = request.getHeader("userId");
		String token = request.getHeader("userToken");
		StringBuilder log = new StringBuilder("manage request info: url:" + request.getRequestURI());
		log.append(" userId:" + userId).append("manage userToken:" + token);
		logger.error("userAuth fail, log = " + log.toString());
		printError(response);
		return false;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		super.afterCompletion(request, response, handler, ex);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
	}

	private boolean checkUserLogin(HttpServletRequest request) {
		
		String uri = request.getRequestURI();
		
		//内部地址  非内部调用拦截
		if (inners.length > 0) {
			for (String exclude : inners) {
				if (uri.matches(exclude)) {
					return false;
				}
			}
		}
		
		String userId = request.getHeader("userId");
		Role role=roleService.getByCode("0002");
		UserRole ur=userRoleService.find(userId, role.getUuid());
		if(ur!=null){
			return true;
		}
		return false;
	}

	private void printError(HttpServletResponse response) {
		try {
			response.getWriter().write(JSON.toJSONString(JsonResponse.fail(ErrorCode.INVALID_TOKEN).getBody()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
