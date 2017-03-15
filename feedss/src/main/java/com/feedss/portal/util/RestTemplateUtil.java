package com.feedss.portal.util;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link RestTemplate} 工具化封装
 * @author Looly
 *
 */
@Slf4j
public class RestTemplateUtil {
	
	/**
	 * 请求JSON结果
	 * @param url 请求路径
	 * @param param 参数
	 * @param requestInterceptor
	 * @return
	 */
	public static JSONObject postForJSON(String url, Object param, ClientHttpRequestInterceptor requestInterceptor){
		return post(url, param, JSONObject.class, requestInterceptor);
	}

	/**
	 * POST请求<br>
	 * @param <T> 返回类型
	 * 
	 * @param url 请求路径
	 * @param param 参数
	 * @param responseType
	 * @param requestInterceptor
	 */
	public static <T> T post(String url, Object param, Class<T> responseType, ClientHttpRequestInterceptor requestInterceptor) {
		final RestTemplate template = new RestTemplate();
		
		//添加转换器
		List<HttpMessageConverter<?>> messageConverters = template.getMessageConverters();
		messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
		messageConverters.add(new FastJsonHttpMessageConverter());
		template.setMessageConverters(messageConverters);
		
		//添加过滤器
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
		interceptors.add(requestInterceptor);
		template.setInterceptors(interceptors);
		
		T info = null;
		try {
			info = template.postForObject(url, param, responseType);
		} catch (RestClientException e) {
			log.info("请求失败：" + url, e);
			return null;
		}
		return info;
	}
}
