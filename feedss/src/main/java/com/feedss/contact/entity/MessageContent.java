package com.feedss.contact.entity;

import java.io.Serializable;

import lombok.Data;

/**
 * 消息元素，文本、表情、自定义 三种定义了，还剩下：位置、图片、音频、文件未定义
 * @author zhouyujuan
 *
 */
@Data
public class MessageContent  implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 6337535901266907899L;

	public enum MessageContentType{
		TIMTextElem,   //文本消息
		TIMLocationElem, //地理消息
		TIMFaceElem, //表情消息
		TIMCustomElem,  //自定义消息
		TIMSoundElem,  //语音消息。（服务端集成Rest API不支持发送该类消息）
		TIMImageElem, //图像消息。（服务端集成Rest API不支持发送该类消息）
		TIMFileElem, //文件消息。（服务端集成Rest API不支持发送该类消息）
	}
	
	// 对象通用字段
	private long id;// 数据库自增ID

	private String uuid;// 内容唯一ID
	
	private String type;

}
