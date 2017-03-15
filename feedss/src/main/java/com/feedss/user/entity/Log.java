package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 日志
 * 
 * @author tangjun
 *
 */
@Entity
@Data
public class Log extends BaseEntity {
	
	private String deviceType;
	
	private String deviceOS;
	
	@Column(length = 36)
	private String deviceId;
	@Column(length = 512)
	private String deviceExt;
	
	private String appVersion;
	
	@Column(length = 20)
	private String appChanel;
	
	private String network;
	
	@Column(length = 36)
	private String userId;// 归属用户

	@Column(length = 36)
	private String object;// 操作对象名称

	@Column(length = 36)
	private String objectId;// 操作对象唯一ID

	@Column(length = 36)
	private String action;// 动作：新增、删除、评论
	
	private String duration;// 操作时长，单位秒
	@Column(length = 512)
	private String ext;//原始记录 
}
