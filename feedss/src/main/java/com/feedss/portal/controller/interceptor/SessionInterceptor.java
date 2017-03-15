package com.feedss.portal.controller.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.feedss.portal.util.RestUtil;

/**
 * 将Session放入RestUtil.localSession，以在Rest请求时备用
 * @author Looly
 *
 */
@Component
public class SessionInterceptor extends HandlerInterceptorAdapter {
	@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
			RestUtil.localSession.set(request.getSession());
			return super.preHandle(request, response, handler);
		}
}
