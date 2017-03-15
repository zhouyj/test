package com.feedss.portal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 用于推流的设备
 * @author zhouyujuan
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class PushStreamTerminal extends BaseEntity {

	public enum Status {
		NORMAL, // 正常
		DEL // 删除
	}
	
	public enum Type{
		NORMAL,
		VR,
		SOFT
	}

	@Column(length = 36, nullable = false, updatable = false)
	private String userId;
	
	@Column(length = 256)
	private String publishUrl;
	@Column(length = 36)
	private String streamId;//当前正在直播的stream id

}
