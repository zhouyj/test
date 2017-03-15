package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 权限
 * 
 * @author tangjun
 *
 */
@Entity
@Data
public class Permission extends BaseEntity {
	@Column(length = 36, nullable = true)
	private String parentId;// 父权限对象Id

	@Column(length = 36, nullable = true)
	private String code;// 编码
}
