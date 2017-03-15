package com.feedss.user.service;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by qinqiang on 2016/8/7.
 */
public interface WeChatService {

	public TreeMap<String,String> unifiedOrder(String orderno,int amont,String remoteIp);

	public int updatePaySuccess(String orderNo,String outOrderNo);

	public Map<String,String> queryOrder(String orderNo);

}
