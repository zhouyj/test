package com.feedss.content.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;

/**
 * 异常解析<br>
 * @author wangjingqing
 */
@ControllerAdvice(basePackages={"com.feedss.contentcenter"})
public class ErrorController {
	Log logger = LogFactory.getLog(getClass());

	@ResponseBody
	@ExceptionHandler
	public ResponseEntity<Object> greeting(HttpServletRequest requset,Throwable ex) {
 		//System.out.println(errorInfo(ex));
 		logger.info(errorInfo(ex));
		return JsonResponse.fail(ErrorCode.INTERNAL_FAILURE);
	}

	/**
	 * 错误日志信息<br>
	 * @param ex
	 */
	private String errorInfo(Throwable ex){
		StringBuilder errorLog = new StringBuilder();
		StackTraceElement[] traces = ex.getStackTrace();
		errorLog.append(ex.toString()).append("\n");
		int size = traces.length;
		for(int i = 0;i<size;i++){
			errorLog.append("   ").append(traces[i].toString()).append("\n");
		}
		return errorLog.toString();
	}
}