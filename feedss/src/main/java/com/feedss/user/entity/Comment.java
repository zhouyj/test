package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论
 * @author zhangjie
 *
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment extends BaseEntity {
	
	@Column(length = 36, nullable = false)
	private String userId;// 源用户Id
	
	@Column(length = 36, nullable = false)
	private String object;// 对象名称 comment,image,goods,
	
	@Column(length = 36, nullable = false)
	private String objectId;//对象Id
	
	@Column(length = 100, nullable = true)
	private String title;// 标题，有些评论有标题和内容两部分，是否可以用BaseEntity.name？
	
	@Column(length = 800, nullable = false)
	private String content;//内容，是否可以用BaseEntity.description？
}
