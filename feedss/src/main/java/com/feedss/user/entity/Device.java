package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 设备
 * 
 * @author tangjun
 *
 */
@Entity
@Data
public class Device extends BaseEntity {
	@Column(length = 36, nullable = false)
	private String userId;// 设备归属用户，一个用户有多个设备

	@Column(length = 128, nullable = false)
	private String sn;// 设备自身的唯一ID
}
