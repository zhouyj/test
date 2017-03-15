package com.feedss.base.util;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.feedss.content.core.UserClientHttpRequestInterceptor;
import com.feedss.content.model.DataEntity;

public class RestTemplateUtil {

	static Log logger = LogFactory.getLog(RestTemplateUtil.class);

	/**
	 * POST请求<br>
	 * 
	 * @param url
	 *            请求路径
	 * @param userId
	 *            用户ID
	 * @param param
	 *            参数 JSON
	 * @param T
	 *            返回类型
	 */
	public static <T> DataEntity<T> postRequest(String url, String userId, Object param, Class<T> t) {
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		FastJsonHttpMessageConverter httpMessageConverter = new FastJsonHttpMessageConverter();
		List<MediaType> typeList = new ArrayList<MediaType>();
		typeList.add(MediaType.APPLICATION_JSON_UTF8);
		typeList.add(MediaType.TEXT_HTML);
		typeList.add(MediaType.APPLICATION_OCTET_STREAM);
		httpMessageConverter.setSupportedMediaTypes(typeList);
		messageConverters.add(httpMessageConverter);
		messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
		RestTemplate template = new RestTemplate(messageConverters);
		List<ClientHttpRequestInterceptor> list = new ArrayList<ClientHttpRequestInterceptor>();
		list.add(new UserClientHttpRequestInterceptor(userId, MediaType.APPLICATION_JSON_UTF8_VALUE));
		template.setInterceptors(list);
		DataEntity<T> reVal = new DataEntity<T>();// 返回数据
		try {
			JSONObject info = template.postForObject(url, param, JSONObject.class);
			if (info == null) {
				logger.debug("get user profile, empty");
				reVal.setCode(99999);
				return reVal;
			}
			reVal.setCode(info.getInteger("code"));
			reVal.setMsg(info.getString("msg"));
			reVal.setData(JSONObject.parseObject(info.getJSONObject("data").toJSONString(), t));
		} catch (RestClientException e) {
			logger.info("请求失败：" + url, e);
			reVal.setCode(99999);
			reVal.setMsg("请求异常...");
			return reVal;
		}
		return reVal;
	}

	/**
	 * POST请求<br>
	 * 
	 * @param url
	 *            请求路径
	 * @param param
	 *            参数
	 * @param requestInterceptor
	 */
	public static void deleteRequest(String url, Object param) {
		RestTemplate template = new RestTemplate();
		List<HttpMessageConverter<?>> messageConverters = template.getMessageConverters();
		messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
		messageConverters.add(new FastJsonHttpMessageConverter());
		template.setMessageConverters(messageConverters);
		
		try {
			template.delete(url, param); 
		} catch (RestClientException e) {
			logger.info("请求失败：" + url, e);
		}
	}
	

	/**
	 * POST请求<br>
	 * 
	 * @param url
	 *            请求路径
	 * @param param
	 *            参数
	 * @param requestInterceptor
	 */
	public static String getRequest(String url, Object param, ClientHttpRequestInterceptor requestInterceptor) {
		RestTemplate template = new RestTemplate();
		List<HttpMessageConverter<?>> messageConverters = template.getMessageConverters();
		messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
		messageConverters.add(new FastJsonHttpMessageConverter());
		template.setMessageConverters(messageConverters);
		if(requestInterceptor!=null){
			List<ClientHttpRequestInterceptor> list = new ArrayList<ClientHttpRequestInterceptor>();
			list.add(requestInterceptor);
			template.setInterceptors(list);
		}
		
		String info = null;
		try {
			info = template.getForObject(url, String.class, param);
		} catch (RestClientException e) {
			logger.info("请求失败：" + url, e);
			return e.getMessage();
		}
		return info;
	}
	
	/**
	 * POST请求<br>
	 * 
	 * @param url
	 *            请求路径
	 * @param param
	 *            参数
	 * @param requestInterceptor
	 */
	public static String postRequest(String url, Object param, ClientHttpRequestInterceptor requestInterceptor) {
		RestTemplate template = new RestTemplate();
		List<HttpMessageConverter<?>> messageConverters = template.getMessageConverters();
		messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
		messageConverters.add(new FastJsonHttpMessageConverter());
		template.setMessageConverters(messageConverters);
		if(requestInterceptor!=null){
			List<ClientHttpRequestInterceptor> list = new ArrayList<ClientHttpRequestInterceptor>();
			list.add(requestInterceptor);
			template.setInterceptors(list);
		}
		
		String info = null;
		try {
			info = template.postForObject(url, param, String.class);
		} catch (RestClientException e) {
			logger.info("请求失败：" + url, e);
			return e.getMessage();
		}
		return info;
	}

	public static String post(String url, String body, String userId, String userToken) {
		HttpHeaders headers = new HttpHeaders();
		if (userId != null)
			headers.add("userId", userId);
		if (userToken != null)
			headers.add("userToken", userToken);
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.add("Accept", MediaType.APPLICATION_JSON_UTF8.toString());
		return post(url, body, headers);
	}

	public static String post(String url, String body, HttpHeaders headers) {
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> formEntity = new HttpEntity<String>(body, headers);
		String resp = restTemplate.postForObject(url, formEntity, String.class);
		return resp;
	}

	/**
	 * GET请求<br>
	 ** 
	 * @param url
	 *            请求路径
	 * @param userId
	 *            用户ID
	 * @param param
	 *            参数 key-value
	 * @param T
	 *            返回类型
	 */
	public static <T> DataEntity<T> getRequest(String url, String userId, Map<String, Object> param, T t) {
		RestTemplate template = new RestTemplate();
		List<HttpMessageConverter<?>> messageConverters = template.getMessageConverters();
		messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
		messageConverters.add(new FastJsonHttpMessageConverter());
		template.setMessageConverters(messageConverters);
		List<ClientHttpRequestInterceptor> list = new ArrayList<ClientHttpRequestInterceptor>();
		list.add(new UserClientHttpRequestInterceptor(userId, null));
		template.setInterceptors(list);

		DataEntity<T> reVal = null;// 返回数据
		try {
			JSONObject info = template.getForObject(url, JSONObject.class, param);
			reVal = JSONObject.parseObject(info.toJSONString(), new TypeReference<DataEntity<T>>() {
			});
		} catch (RestClientException e) {
			logger.info("请求失败：" + url, e);
			reVal = new DataEntity<T>();
			reVal.setCode(99999);
			reVal.setMsg("请求异常...");
		}
		return reVal;
	}

	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}
