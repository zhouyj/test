package com.feedss.base.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;


public class CommonUtil {

	static CustomLinkedHashMap<String,Object> keyMap=new CustomLinkedHashMap<String,Object>();
	
    public final static String GROUP_PRIFIX = "GROUP";
	
	/**
	 * 同步锁Object
	 * @param key
	 * @return
	 */
	public synchronized static Object getKey(String key){
		Object v=null;
		if((v=keyMap.get(key))==null){
			v=new Object();
			keyMap.put(key, v);
		}
		return v;
	}

    public static String getGroupId(String streamId) {
        return GROUP_PRIFIX + streamId;
    }
    
    public static String getStreamId(String groupId){
    	return groupId.substring(GROUP_PRIFIX.length());
    }
	
	private static Logger log = LoggerFactory.getLogger(CommonUtil.class);
	public static JSONObject httpsRequestToJsonObject(String requestUrl, String requestMethod, String outputStr) {
		JSONObject jsonObject = null;
		try {
			StringBuffer buffer = httpsRequest(requestUrl, requestMethod, outputStr);
			jsonObject = JSONObject.parseObject(buffer.toString());
		}  catch (Exception e) {
			log.error("https请求异常："+e.getMessage());
		}
		return jsonObject;
	}


	private static StringBuffer httpsRequest(String requestUrl, String requestMethod, String output)
			throws NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException, MalformedURLException,
			IOException, ProtocolException, UnsupportedEncodingException {

		URL url = new URL(requestUrl);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestMethod(requestMethod);
		if (null != output) {
			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(output.getBytes("UTF-8"));
			outputStream.close();
		}

		// 从输入流读取返回内容
		InputStream inputStream = connection.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		String str = null;
		StringBuffer buffer = new StringBuffer();
		while ((str = bufferedReader.readLine()) != null) {
			buffer.append(str);
		}

		bufferedReader.close();
		inputStreamReader.close();
		inputStream.close();
		inputStream = null;
		connection.disconnect();
		return buffer;
	}
}

class CustomLinkedHashMap<K,V> extends LinkedHashMap<K,V>{
	private static final int MAX_ENTRIES = 10000;
	 
	protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
	    // 当前记录数大于设置的最大的记录数，删除最旧记录（即最近访问最少的记录）
	    return size() > MAX_ENTRIES;
	}
	
}
