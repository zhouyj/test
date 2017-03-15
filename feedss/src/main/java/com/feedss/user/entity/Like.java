package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 点赞
 * 
 * @author tangjun
 *
 */
@Table(name="t_like")
@Entity
@Data
public class Like extends BaseEntity {
	
	public enum ListType{
		Unlimited,//无限制
		Limit//限制
	}
	
	@Column(length = 36, nullable = false)
	private String userId;// 归属用户

	@Column(length = 36, nullable = false)
	private String object;// 点赞对象名称，如用户、直播流、图片内容等

	@Column(length = 36, nullable = false)
	private String objectId;// 点赞对象uuid

	private long count;// 点赞次数
}
