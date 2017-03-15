package com.feedss.contact.entity;

import lombok.Data;

/**
 * 自定义消息元素
 * @author zhouyujuan
 *
 */
@Data
public class CustomMessageContent extends MessageContent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7675732134049359523L;

	private String data;//额外数据，不会进入ios apns

	private String desc; // 自定义消息描述信息； ios push
	
	private String ext; //扩展字段；json串
	
//	private String sound = "dingdong.aiff"; //APNS推送铃音
	
	public CustomMessageContent(){
		this.setType(MessageContentType.TIMCustomElem.name());
	}
}
