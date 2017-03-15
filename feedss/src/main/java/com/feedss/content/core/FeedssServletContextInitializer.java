package com.feedss.content.core;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.stereotype.Component;

import com.feedss.content.monitor.RoomKeyinitialize;

/**
 * 初始化操作<br>
 * @author wangjingqing
 *
 */
@Component
public class FeedssServletContextInitializer implements ServletContextInitializer {

	
	@Resource(name="roomKeyinitialize")
	private RoomKeyinitialize roomKeyinitialize;
	
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		roomKeyinitialize.start();//初始化redis中的room当监控中
	}
}
