package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
/**
 * 点卡，引导用户注册
 * @author zhouyujuan
 *
 */
@Entity
@Data
public class TimeCard {

	public enum TimeCardStatus{
		UNACTIVIED //未激活
		, ACTIVIED  //激活
		, INVALID   //无效
	}
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition=" int(20) not null  AUTO_INCREMENT ",unique=true,updatable=false)
	private long id;
	private String userId;
	private String serialNumber;
	private String password;
	private int expired; //积分有效期
	private int points;
	private String description; //描述
	private long activeTime;
	private long createTime;
	private int status;
}
