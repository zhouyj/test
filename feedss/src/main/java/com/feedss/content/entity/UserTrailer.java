package com.feedss.content.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 用户预定<br>
 * @author wangjingqing
 * @date 2016-07-29
 */
@Entity
@Data
@EqualsAndHashCode(callSuper=true)
public class UserTrailer extends BaseEntity{
	
	@Column(length = 36)
	private String category;//分类

	@Column(length=100)
	private String trailerId;//预告uuid
	
	@Column(nullable=false)
	private String userId;//用户uuid
	
	@Column(nullable=false)
	private boolean isDelete;//用于逻辑删除 false：可用  true：删除
	
}
