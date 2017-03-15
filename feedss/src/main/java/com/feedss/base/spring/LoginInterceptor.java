package com.feedss.base.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.util.RedisUtil;
import com.feedss.base.util.RedisUtil.KeyType;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.manage.util.Constants;
import com.feedss.user.entity.User.UserStatus;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.UserService;

/**
 * 登录拦截
 *
 */
@Service
public class LoginInterceptor extends HandlerInterceptorAdapter {
	private final String[] excludes = new String[] { 
			"/user/thirdpartyLogin", 
			"/user/start",
			"/user/login", 
			"/user/pwdLogin",
			"/user/sendVerifyCode", // "/user/offlineStatistics",
			"/pay/wechat/paynotify", 
			"/user/verifyToken", 
			"/user/version/newVersion",
			"/hhh/token", 
			"/error", 
			"/user/registerByMobile", 
			"/group/.*",
			"/connect/.*", 
			"/message/sendNotification", 
			"/message/sendSystemMsg", 
			"/callback.*",
			"/h5/.*",
			"/login",
			"/",
			"/sign",
			"/list",
			"/lives",
			"/livePlay",
			"/article",
			"/signupByUserPwd",
			"/resetPwdPage",
			"/signout",
			"/improveInfoPage",
			"/signupFinish",
			"/profile",
			"/bindCardPage",
			"/bindMobilePage",
			"/changePwdPage",
			"/editInfoPage",
			"/shop",
			"/shop/.*",
			"/portal",
			"/portal/.*",
			"/stream/importVideo",
	};

	private final String[] inners = new String[] { "/user/offlineStatistics" };

	@Autowired
	RedisUtil redisUtil;
	
	@Autowired
	ConfigureUtil configureUtil;

	@Autowired
	UserService userService;

	Logger logger = Logger.getLogger(LoginInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String uri = request.getRequestURI();
		
		// 管理后台
		if(uri.contains("/manage")){
			HttpSession session=request.getSession();
			setActivt(request);
			setTitle(request);
			if (excludes.length > 0) {
				for (String exclude : excludes) {
					if (uri.matches(exclude)) {
						return true;
					}
				}
			}
			UserVo user=(UserVo)session.getAttribute(Constants.USER_SESSION);
			if(user==null){
				response.sendRedirect("/login");
				return false;
			}else{
				
				ErrorCode code = checkUserLogin(request.getRequestURI(), user.getUuid(), user.getToken());
				if(code != ErrorCode.SUCCESS){
					response.getOutputStream().print("<script>alert('" + code.getMsg() + "');location.href='/login'</script>");
					return false;
				}
			}
			return true;
		}

		// 按地址过滤
		if (excludes.length > 0) {
			for (String exclude : excludes) {
				if (uri.matches(exclude)) {
					return true;
				}
			}
		}
		
		// 检查登录token
		String userId = request.getHeader("userId");
		String token = request.getHeader("userToken");
		ErrorCode code = checkUserLogin(request.getRequestURI(), userId, token);
		if (code == ErrorCode.SUCCESS) {
			return true;
		}

		//打印认证错误日志
		StringBuilder log = new StringBuilder("request info: url:" + request.getRequestURI());
		log.append(" userId:" + userId).append(" userToken:" + token);
		logger.error("userAuth fail, log = " + log.toString());
		printError(response, code);
		
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

	private ErrorCode checkUserLogin(String uri, String userId, String userToken) {
		// 内部地址放行，非内部调用拦截
		if (inners.length > 0) {
			for (String exclude : inners) {
				if (uri.matches(exclude)) {
					return ErrorCode.NO_AUTH;
				}
			}
		}
		
		// 检查用户
		if (StringUtils.isNotEmpty(userToken) && StringUtils.isNotEmpty(userId)) {
			UserVo userVo = userService.getUserVoByUserId(userId);
			if (userVo == null)
				return ErrorCode.USER_NOT_EXIST;
			else if (userVo.getStatus() == UserStatus.DISABLED.ordinal())
				return ErrorCode.USER_FORBIDDEN;
			String token = redisUtil.get(KeyType.TOKEN, userId);
			if (userToken.equals(token)) {
				return ErrorCode.SUCCESS;
			}
		}
		return ErrorCode.INVALID_TOKEN;
	}

	private void printError(HttpServletResponse response, ErrorCode code) {
		try {
			response.getWriter().write(JSON.toJSONString(JsonResponse.fail(code).getBody()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 用来设置left激活状态<br>
	 * @param request
	 */
	private void setActivt(HttpServletRequest request){
		String type = request.getParameter("activeLeftType");
		request.getSession().setAttribute("activeLeftType", type);
		request.setAttribute("cloudUrl", configureUtil.getConfig(com.feedss.base.Constants.CLOUD_URL));
	}

	/**
	 * 设置页面标题
	 * @param request
	 */
	private void setTitle(HttpServletRequest request) {
		request.setAttribute("pageTitle", configureUtil.getConfigureValue("site_title"));
	}

}
