package com.feedss.contact.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "groups")
public class Group{

	public enum GroupType{
		Public, Private, Chatroom, AVChatRoom
	}
	
	@Id
	@Column(length = 48, unique = true, nullable = false)
	private String uuid;

	@Column(length = 10, nullable = false)
	private String type; // GroupType
	
	@Column(length = 30, nullable = false)
	private String name; 
	
	@Column(length = 120)
	private String introduction;

	@Column(length = 150)
	private String notification;
	
	@Column(length = 100)
	private String faceUrl;
	
	@Column(length = 36)
	private String ownerAccount;
	
	@Column
	private Date createTime;

	@Column
	private long infoSeq; // 群资料的每次变都会增加该值

	@Column
	private Date lastInfoTime; //群组最后一次信息变更时间

	@Column
	private Date lastMsgTime;//群组内最后发消息的时间

	@Column
	private long nextMsgSeq;//群内下一条消息的Seq

	@Column
	private int memberNum;//当前成员数量

	@Column
	private int maxMemberNum;//最大成员数量

	@Column
	private String applyJoinOption;//申请加群选项

	
}
