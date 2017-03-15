package com.feedss.content.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.Constants;
import com.feedss.base.util.RestTemplateUtil;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.portal.entity.PushStreamTerminal;

/**
 * 与srs服务打通
 * @author zhouyujuan
 *
 */
@Component
public class SrsService {
	
	Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private ConfigureUtil configureUtil;
	
	public void pauseTerminalPublish(PushStreamTerminal t){
		if(t==null) return;
		String terminalName = t.getPublishUrl().substring(t.getPublishUrl().lastIndexOf("/")+1);
		pauseTerminalPublish(terminalName, configureUtil.getConfig(Constants.APP_NAME));
	}
	
	public void pauseTerminalPublish(String terminalName, String appName){
		String api = configureUtil.getConfig(Constants.SRS_API) + "/api/v1/streams";
		String resp = "";
		try{
			resp = RestTemplateUtil.getRequest(api, null, null);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(StringUtils.isEmpty(resp)){
			logger.error("get nothing from srs");
			return;
		}
		logger.info("get something from srs, " + resp);
		JSONObject result = JSONObject.parseObject(resp);
		if(result==null){
			logger.error("get something from srs is not json, " + resp);
			return;
		}
		Integer code = result.getInteger("code");
		if(code!=null && code == 0){
			JSONArray streams = result.getJSONArray("streams");
			if(streams!=null && !streams.isEmpty()){
				for(int i=0;i<streams.size();i++){
					JSONObject stream = streams.getJSONObject(i);
					String name = stream.getString("name");
					String app = stream.getString("app");
					if(appName.equalsIgnoreCase(app) && terminalName.equalsIgnoreCase(name)){
						JSONObject publish = stream.getJSONObject("publish");
						if(publish!=null && publish.getBooleanValue("active")){
							int cid = publish.getIntValue("cid");
							logger.info("find cid = " + cid + ", to kick off");
							String delApi = configureUtil.getConfig(Constants.SRS_API) + "/api/v1/clients/" + cid;
							RestTemplateUtil.deleteRequest(delApi, null);
							logger.info("after kick off terminal, " + terminalName);
						}
					}
				}
			}
		}
	}
	
}
