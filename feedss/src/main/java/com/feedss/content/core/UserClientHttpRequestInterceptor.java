package com.feedss.content.core;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * 请求头增加用户信息
 * @author wangjingqing
 * @data 2016-08-04
 */
public class UserClientHttpRequestInterceptor implements ClientHttpRequestInterceptor{

	private String userId;
	
	private String contentType;
	
	private String from = null;
	public UserClientHttpRequestInterceptor(String userId,String contentType){
		this.userId = userId;
		this.contentType = contentType;
		this.from = "inner";
	}
	public UserClientHttpRequestInterceptor(String userId){
		this.userId = userId;
	}
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)throws IOException {
		request.getHeaders().add("userId",userId);
		request.getHeaders().add("from",from);//内部调用标识 用于过滤鉴权
		if(contentType != null){
			//设置HTTP请求的请求头信息  
			request.getHeaders().setContentType(MediaType.parseMediaType(contentType));
		}
        //设置相应内容，相应内容将被转换为json格式返回  
		//request.getHeaders().setAcceptCharset(Collections.singletonList(Charset.forName("ISO-8859-1")));  
		//request.getHeaders().setAccept(Collections.singletonList(MediaType.APPLICATION_JSON)); 
		return execution.execute(request, body);
	}

}
