package com.feedss.portal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.feedss.base.Row;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by shenbingtao on 2016/8/4.
 */
@Slf4j
public class HttpUtil {

	private static DefaultHttpClient httpClient = null;
	private static PoolingClientConnectionManager cm = null;

	private static final Integer CONNECTION_TIMEOUT = 5 * 1000;
	private static final Integer SO_TIMEOUT = 5 * 1000;
	private final static Long CONN_MANAGER_TIMEOUT = 500L;

	private static final Logger logger;

	static {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		cm = new PoolingClientConnectionManager(schemeRegistry);
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(cm.getMaxTotal());// （目前只有一个路由，因此让他等于最大值）
		httpClient = new DefaultHttpClient(cm);
		// httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
		// httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);
		httpClient.getParams().setLongParameter(ClientPNames.CONN_MANAGER_TIMEOUT, CONN_MANAGER_TIMEOUT);
		// 在提交请求之前 测试连接是否可用
		// httpClient.getParams().setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
		httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, true));
		logger = LoggerFactory.getLogger(HttpUtil.class);
	}

	public static HttpClient getInstance() {
		return httpClient;
	}

	public static <T> T doPostBody(String url, Map params, String encoding, Class<T> t) {
		log.info("request url:" + url + ", params:" + JSON.toJSONString(params));
		try {
			String json = postUrlWithBody(url, JSON.toJSONString(params), encoding);
			if (StringUtils.isNotEmpty(json)) {
				log.info("response:" + json);
				return JSON.parseObject(json, t);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static <T> T doGet(String url, Row params, String encoding, Class<T> t, Row headers) {
		log.info("request url:" + url + ", params:" + JSON.toJSONString(params) + ", headers:" + JSON.toJSONString(headers));
		try {
			String json = getUrlWithParams(url, params, encoding, headers);
			if (StringUtils.isNotEmpty(json)) {
				log.info("response:" + json);
				return JSON.parseObject(json, t);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static <T> T doPost(String url, Map params, String encoding, Class<T> t) {
		log.info("request url:" + url + ", params:" + JSON.toJSONString(params));
		try {
			String json = postUrlWithParams(url, params, encoding);
			if (StringUtils.isNotEmpty(json)) {
				log.info("response:" + json);
				return JSON.parseObject(json, t);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String postUrlWithParams(String url, Map params, String encoding) throws Exception {

		HttpPost httpost = new HttpPost(url);
		// 添加参数
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (params != null && params.keySet().size() > 0) {
			Iterator iterator = params.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				nvps.add(new BasicNameValuePair((String) entry.getKey(), (String) entry.getValue()));
			}
		}

		httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

		HttpResponse response = getInstance().execute(httpost);
		HttpEntity entity = response.getEntity();

		return dump(entity, encoding);
	}

	public static String postUrlWithBody(String url, String body, String encoding) throws Exception {

		HttpPost httpost = new HttpPost(url);
		httpost.setEntity(new StringEntity(body, Consts.UTF_8));
		httpost.setHeader("Content-Type", "application/json;charset=UTF-8");

		HttpResponse response = getInstance().execute(httpost);
		HttpEntity entity = response.getEntity();

		return dump(entity, encoding);
	}

	private static String dump(HttpEntity entity, String encoding) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), encoding));

		String result = br.readLine();
		br.close();
		return result;
	}

	public static String postUrlWithBody(String url, String body, String encoding, Row headers) throws Exception {

		HttpPost httpost = new HttpPost(url);
		httpost.setEntity(new StringEntity(body, Consts.UTF_8));
		for (String key : headers.keySet()) {
			httpost.setHeader(key, headers.get(key).toString());
		}
		httpost.setHeader("Content-Type", "application/json;charset=UTF-8");

		HttpResponse response = getInstance().execute(httpost);
		HttpEntity entity = response.getEntity();

		return dump(entity, encoding);
	}

	public static <T> T doPostBody(String url, Map params, String encoding, Class<T> t, Row headers) {
		log.info("request url:" + url + ", params:" + JSON.toJSONString(params));
		try {
			String json = postUrlWithBody(url, JSON.toJSONString(params), encoding, headers);
			if (StringUtils.isNotEmpty(json)) {
				log.info("response:" + json);
				return JSON.parseObject(json, t);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String getUrlWithParams(String url, Row params, String encoding, Row headers) throws Exception {

		StringBuilder sb = new StringBuilder(url);
		if (params != null) {
			for (String key : params.keySet()) {
				if (url.contains("?")) {
					sb.append("&" + key + "=" + params.gets(key));
				} else {
					sb.append("?" + key + "=" + params.gets(key));
				}
			}
		}
		HttpGet httGet = new HttpGet(sb.toString());
		for (String key : headers.keySet()) {
			httGet.setHeader(key, headers.get(key).toString());
		}
		HttpResponse response = getInstance().execute(httGet);
		HttpEntity entity = response.getEntity();

		return dump(entity, encoding);
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
