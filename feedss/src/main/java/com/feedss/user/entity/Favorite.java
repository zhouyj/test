package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 收藏
 * 
 * @author tangjun
 *
 */
@Entity
@Data
public class Favorite extends BaseEntity {
	
	public enum FavoriteType{
		FAVORITE //收藏
		, VIEW  //浏览
	}
	
	@Column(length = 36, nullable = false)
	private String userId;// 用户Id

	@Column(length = 36, nullable = false)
	private String object;// 对象名称：如user，content，url，stream

	@Column(length = 36, nullable = false)
	private String objectId;// 被收藏的对象Id
}
