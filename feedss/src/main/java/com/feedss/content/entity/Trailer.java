package com.feedss.content.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预告<br>
 * @author wangjingqing
 * @date 2016-07-20
 */
@Entity
@Data
@EqualsAndHashCode(callSuper=true)
public class Trailer extends BaseEntity{

	public enum TrailerStatus{
		Available,//可用
		Unavailable//不可用
	}
	@Column(length=100)
	private String title;//标题
	
	@Column
	private String streamId;//直播uuid
	
	@Column
	private Date playTime;//播放时间
	
	@Column(length=200)
	private String picUrl;//图片路径
	
	@Column(nullable=false)
	private String userId;//用户UUID
	
	@Column
	private String content;//内容简介
	
	@Column
	private Date aheadTime;//预热时间 
	
	@Column
	private boolean isSendMsg;//是否发送消息 true：发送 false：不发送
	
	@Transient
	private String userNickname;// 用户昵称
	
	@Transient
	private String playTiemStr;// 
	@Transient
	private int trStatus;
}
