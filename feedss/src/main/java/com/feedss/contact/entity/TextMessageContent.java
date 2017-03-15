package com.feedss.contact.entity;

import lombok.Data;

/**
 * 文本消息元素
 * @author zhouyujuan
 *
 */
@Data
public class TextMessageContent extends MessageContent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4567520141695019782L;
	private String text;// 消息内容 ios push内容
	
	public TextMessageContent(){
		this.setType(MessageContentType.TIMTextElem.name());
	}

}
