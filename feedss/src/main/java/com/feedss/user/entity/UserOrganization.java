package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 用户组织
 * 
 * @author tangjun
 *
 */
@Entity
@Data
public class UserOrganization extends BaseEntity {
	@Column(length = 36, nullable = false)
	private String userId;// 用户Id

	@Column(length = 36, nullable = false)
	private String organizationId;// 组织Id
}
