package com.feedss.base.util;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.joda.time.DateTime;

public class WeChatPayUtil {
	/**
	 * 微信支付签名
	 * 
	 * @param apiparamsMap
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String md5Sign(Map<String, String> apiparamsMap, String key) throws Exception {
		StringBuffer orgin = new StringBuffer();
		String sign = "";
		Map<String, String> treeMap = new TreeMap<String, String>();
		treeMap.putAll(apiparamsMap);
		Iterator<String> iter = treeMap.keySet().iterator();
		while (iter.hasNext()) {
			String name = (String) iter.next();
			if (name.equals("sign")) {
				continue;
			}
			orgin.append("&" + name + "=").append(apiparamsMap.get(name));
		}
		orgin.append("&key=" + key);
		sign = orgin.toString();
		sign = sign.substring(1, sign.length());
		MessageDigest md = MessageDigest.getInstance("MD5");
		sign = byte2hex(md.digest(sign.getBytes("UTF-8"))).toUpperCase();
		return sign;
	}

	private static String byte2hex(byte[] b) {
		StringBuffer hs = new StringBuffer();
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs.append("0").append(stmp);
			else
				hs.append(stmp);

		}
		return hs.toString().toUpperCase();
	}

	/**
	 * 将map转换为xml
	 *
	 * @param parmas
	 * @return
	 */
	public static String map2Xml(Map<String, String> parmas) {
		StringBuffer xml = new StringBuffer();
		for (Map.Entry<String, String> en : parmas.entrySet()) {
			String value = en.getValue();
			xml.append("<" + en.getKey() + ">" + value + "</" + en.getKey() + ">");
		}
		return xml.toString();
	}

	/**
	 * 将xml转换为map
	 *
	 * @param xml
	 * @return
	 */
	public static Map<String, String> xml2Map(String xml) {
		Map<String, String> result = new HashMap<String, String>();
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(xml);
			Element rootEle = doc.getRootElement();
			Iterator<Element> iter = rootEle.elements().iterator();
			while (iter.hasNext()) {
				Element ele = iter.next();
				String name = ele.getName();
				String value = ele.getText();
				result.put(name, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String create_nonce_str() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public static String randomOrderNum() {
		String time = new DateTime().toString("yyyyMMddHHmmss");
		int m = (int) ((Math.random() * 9 + 1) * 100000);
		return time + m;
	}

}
