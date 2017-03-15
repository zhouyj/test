package com.feedss.portal.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.feedss.base.Constants;

/**
 * Created by shenbingtao on 2016/7/24.
 */
@Controller
public class BaseController {

	/**
	 * 基于@ExceptionHandler异常处理
	 */
	@ExceptionHandler(Exception.class)
	public String exp(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
		ex.printStackTrace();
		return "/error/500";
	}

	protected String returnJSONP(String callback, String out) {
		return "<script type='text/javascript'>" + callback + "(" + out + ")</script>";
	}

	protected String getUserIdFromHeader(HttpServletRequest request) {
		return (String) request.getHeader(Constants.PASSPORT_USERID);
	}

	protected String getUserId(HttpServletRequest request) {
		// return (String)request.getAttribute(Constants.PASSPORT_USERID);
		return "10000";// test
	}

}
