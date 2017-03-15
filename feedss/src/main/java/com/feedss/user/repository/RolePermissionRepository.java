package com.feedss.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.RolePermission;

/**
 * 角色权限
 * @author zhouyujuan
 *
 */
public interface RolePermissionRepository extends BaseRepository<RolePermission> {

	
	@Query("select rp from RolePermission as rp where rp.roleId=:roleId")
	public List<RolePermission> find(@Param("roleId")String roleId);
	
	
}
