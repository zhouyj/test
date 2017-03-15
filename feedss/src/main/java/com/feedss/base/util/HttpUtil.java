package com.feedss.base.util;

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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.alibaba.fastjson.JSON;
import com.feedss.base.Row;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by shenbingtao on 2016/8/4.
 */
@Slf4j
public class HttpUtil {

	// private static final DefaultHttpClient httpclient = new
	// DefaultHttpClient();

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
			log.error("has error", ex);
		}
		return null;
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
			log.error("has error", ex);
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
			log.error("has error", ex);
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
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(httpost);
		HttpEntity entity = response.getEntity();

		return dump(entity, encoding);
	}

	public static String postUrlWithBody(String url, String body, String encoding) throws Exception {

		HttpPost httpost = new HttpPost(url);
		httpost.setEntity(new StringEntity(body, Consts.UTF_8));
		httpost.setHeader("Content-Type", "application/json;charset=UTF-8");
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(httpost);
		HttpEntity entity = response.getEntity();

		return dump(entity, encoding);
	}

	public static String postUrlWithBody(String url, String body, String encoding, Row headers) throws Exception {

		HttpPost httpost = new HttpPost(url);
		httpost.setEntity(new StringEntity(body, Consts.UTF_8));
		if (headers != null) {
			for (String key : headers.keySet()) {
				httpost.setHeader(key, headers.gets(key));
			}
		}
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(httpost);
		HttpEntity entity = response.getEntity();

		return dump(entity, encoding);
	}

	private static String dump(HttpEntity entity, String encoding) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), encoding));
		String result = br.readLine();
		br.close();
		return result;
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
