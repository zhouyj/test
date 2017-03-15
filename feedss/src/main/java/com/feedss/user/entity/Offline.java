package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;
/**
 * 离线
 * @author 张杰 
 *
 */
@Entity
@Data
public class Offline extends BaseEntity{
	@Column(length = 36, nullable = false)
	private String userId;// 归属用户

	@Column(length = 36, nullable = false)
	private String object;// 操作对象名称

	@Column(length = 36, nullable = false)
	private String objectId;// 操作对象唯一ID

	@Column(length = 36, nullable = false)
	private String action;// 动作：新增、删除、评论
	
	private long duration;// 操作时长，单位秒
	
	private String ext;//原始记录 
	
	private Integer integral;//积分
}
