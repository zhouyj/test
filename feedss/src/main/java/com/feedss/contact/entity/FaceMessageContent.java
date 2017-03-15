package com.feedss.contact.entity;

import lombok.Data;

/**
 * 表情消息元素
 * @author zhouyujuan
 *
 */
@Data
public class FaceMessageContent extends MessageContent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4622971421470686257L;
	private int index;//表情索引
	private String data;//额外数据

	public FaceMessageContent(){
		this.setType(MessageContentType.TIMFaceElem.name());
	}
}
