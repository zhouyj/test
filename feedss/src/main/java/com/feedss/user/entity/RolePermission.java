package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 角色的权限
 * 
 * @author tangjun
 *
 */
@Entity
@Data
public class RolePermission extends BaseEntity {
	@Column(length = 36, nullable = false)
	private String roleId;// 角色唯一Id

//	@Column(length = 36, nullable = false)
//	private String permissionId;// 权限唯一Id
	

	@OneToOne
	@JoinColumn(name = "permissionId", foreignKey = @ForeignKey(name = "none"))
	Permission permission;
}
