package com.feedss.content.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 事件
 * @author zhouyujuan
 *
 */
@Entity
@Data
@EqualsAndHashCode(callSuper=true)
public class Event extends BaseEntity {

	public enum EventStatus{
		DRAFT, PUBLISHED, RUBBISH, DELETED
	}

	private Date started;// 开始时间
	private Date ended;// 结束时间
	
	@Column(length = 128)
	private String address;//活动地点
	
	@Column(length = 128)
	private String cover;// 封面图片URI
	
	@Column(length = 36)
	private String streamId;
	
	@Column(length = 128)
	private String schedulePic;//活动议程
	
	@Column(length = 128)
	private String contact;//联系我们

	//参与嘉宾、赞助商、合作伙伴  都采用相同的结构 Map<key, List<Map<String, String>>,item：name,pic,description
	//key:hosts, sponsors, partners
	@Column(columnDefinition="TEXT")
	private String ext;

	@Column
	private int viewCount;//查看次数
}
