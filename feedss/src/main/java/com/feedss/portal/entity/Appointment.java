package com.feedss.portal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 预约
 * 
 * @author tangjun
 *
 */
@Data
@Entity
public class Appointment extends BaseEntity {
	@Column(length = 36, nullable = false)
	private String userId;
	@Column(length = 36, nullable = false)
	private String targetUserId;
	@Column(length = 36, nullable = false)
	private String mobile;
}
