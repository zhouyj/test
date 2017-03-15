package com.feedss.contact.entity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * 消息
 * 
 * @author zhouyujuan
 *
 */
//@Entity
//@Table(name = "messages")
@Data
public class Message implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8136209274291852414L;

	public enum Source {
		Barrage, // 弹幕
		Group, //群聊
		AddGroupMemmber, //有人进组
		GetOutGroupMemmber,//有人离组
		ShutUpGroupMemmber, //禁言某人
		Follow, //关注主播
		SendGift, //送礼物
		GroupSystemMessage, //系统消息
		HostLeft, //主播暂时离开
		HostComeback, //主播回来
		ConnectingRequest, // 连线请求-发主持人
		ConnectingConfirm, // 连线确认
		ConnectingCancel, // 连线断开
		TaskRequest, // 发布任务
		TaskResponse, // 抢任务
		Advertisement, // 广告
		StreamEnded, //直播结束
		RoomInfo, // 直播间数据更新
		AppointmentReminder, //预约提醒
		SystemMessage, //系统消息
		SingleSignOn, //单点登录
		Appointment, //预约
		UserDisable,
		StreamSummary,
		StreamCreate
	}
	
	public enum MessageStatus{
		Draft, //草稿
		Normal, //正常
		Del //标记删除
	}

	public enum ReadStatus{
		UNREAD, //未读
		READ //已读
	}

	@JsonIgnore
	private String uuid;

	private List<MessageContent> msgBody;

//	private String messageSource;

//	private int msgStatus;
	
	private String fromAccount; //发送者id
	
	private String[] toAccount; //接收者id

}
