package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 用户角色
 * 
 * @author tangjun
 *
 */
@Entity
@Data
public class UserRole extends BaseEntity {
	@Column(length = 36, nullable = false)
	private String userId;// 用户Id

	@Column(length = 36, nullable = false)
	private String roleId;// 角色Id
	

	@OneToOne
	@JoinColumn(name = "uuid", foreignKey = @ForeignKey(name = "none"))
	private User user;// 用户Id

	@OneToOne
	@JoinColumn(name = "uuid", foreignKey = @ForeignKey(name = "none"))
	private Role role;// 角色Id
}
