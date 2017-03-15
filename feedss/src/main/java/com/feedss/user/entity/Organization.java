package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 组织
 * 
 * @author tangjun
 *
 */
@Entity
@Data
public class Organization extends BaseEntity {
	@Column(length = 36, nullable = false)
	private String parentId;// 父组织的唯一Id

	@Column(length = 36, nullable = false)
	private String code;// 编码

	@Column(length = 36, nullable = true)
	private String owner;// 归属人userId
}
