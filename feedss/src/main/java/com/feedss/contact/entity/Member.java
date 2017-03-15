package com.feedss.contact.entity;

import lombok.Data;

@Data
public class Member {

	public enum MemberRole{
		Owner, //群主
		Admin, //管理员
		Member,//成员
	}
	
	public enum MemberMessageFlag{
		AcceptAndNotify, //接收并提示
		AcceptNotNotify, //接收不提示，直播间应该采取这种方式
		Discard, // 屏蔽群消息（不会向客户端推送消息）
	}
	
	private String memberAccount;
	private String role;
	private int joinTime;
	private int msgSeq;
	private String msgFlag;
	private int lastSendMsgTime;
	private String nameCard;
}
