package com.feedss.content.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 文章
 * @author zhouyujuan
 *
 */
@Entity
@Data
@EqualsAndHashCode(callSuper=true)
public class Article extends BaseEntity {
	public enum ArticleStatus{
		DELETED, DRAFT, PUBLISHED, RUBBISH
	}

	@Column(length = 36)
	private String category;//分类
	
	@Column(columnDefinition="TEXT")
	private String text;//正文,需要设置column为TEXT
	
	@Column(length = 128)
	private String cover;// 封面图片URI
	
	@Column(length = 1024)
	private String pics;// 图片列表, ","连接
	
	@Column(length = 36)
	private String streamId;
	@Column
	private int viewCount;//查看次数
	
	private String source;//来源

	@Column(length=256)
	private String summary;//摘要
}
