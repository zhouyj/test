package com.feedss.base.spring;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

/**
 * 日志切面
 * 
 */
@Aspect
@Component
public class ControllerLogAop {

	private static final Logger logger = LoggerFactory.getLogger(ControllerLogAop.class);

	@Pointcut("execution(public * com..*Controller.*(..))")
	public void logPoint() {
	}

	// 响应日志
	// 记录执行时间，单位：毫秒
	@Around("logPoint() && args(request,body,..)")
	public Object executeTime(ProceedingJoinPoint joinPoint, HttpServletRequest request, String body) {

		StringBuilder log = new StringBuilder("url:" + request.getRequestURI());

		String userId = request.getHeader("userId");
		String userToken = request.getHeader("userToken");
		String deviceId = request.getHeader("deviceId");
		String deviceAgent = request.getHeader("deviceAgent");
		String deviceType = request.getHeader("deviceType");
		String deviceVersion = request.getHeader("deviceVersion");
		String appChannel = request.getHeader("appChannel");
		String appVersion = request.getHeader("appVersion");

		log.append(", Headers{ userId:" + userId).append(", userToken:" + userToken).append(", deviceId:" + deviceId)
				.append(", deviceAgent:" + deviceAgent).append(", deviceType:" + deviceType)
				.append(", deviceVersion:" + deviceVersion).append(", appChannel:" + appChannel)
				.append(", appVersion:" + appVersion).append("}");
		log.append(", Params{ ");
		Map<String, String[]> parameterMap = request.getParameterMap();
		if (parameterMap != null && !parameterMap.isEmpty()) {

			for (String name : parameterMap.keySet()) {
				String[] values = parameterMap.get(name);
				if (values == null || values.length == 0) {
					return null;
				}
				String value = values[0];
				log.append("(name:" + name + ", value:" + value + ")");
			}
		}
		log.append("}");

		log.append(", Body:" + body);
		String requestStr = log.toString();
		logger.info("request info: " + requestStr);
		log.setLength(0);

		long start = System.currentTimeMillis();
		Object o = null;
		try {
			o = joinPoint.proceed();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		long end = System.currentTimeMillis();
		log.append("execute info: " + requestStr + ", execute time len:" + (end - start) + "ms");
		if (o != null) {
			log.append(", response data:" + JSON.toJSONString(o));
		}
		logger.info(log.toString());
		return o;
	}
}
