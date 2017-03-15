package com.feedss.content.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 观看时长<br>
 * @author wangjingqing
 *
 */
@Entity
@Data
@EqualsAndHashCode(callSuper=true)
public class WatchStream extends BaseEntity{

	public enum WatchType{
		WatchStream,//观看直播
		Stream,//直播
		WatchBack;//观看回放
	}
	
	@Column
	private String streamId;//stream的uuid
	@Column
	private long watchTime;//观看时长(单位秒)
	
}
