package com.feedss.user.service;

import java.util.List;
import java.util.Map;

import com.feedss.user.entity.Role;

/**
 * 角色
 * @author 张杰
 *
 */
public interface RoleService {


	/**
	 * 角色列表
	 * @return
	 */
	List<Role> roleList();

	Role getByUuid(String roleId);
	
	Role getByCode(String roleCode);

	List<Map<String, String>> getRoles(String userId);
	
	List<String> getPermissions(String userId);
}
