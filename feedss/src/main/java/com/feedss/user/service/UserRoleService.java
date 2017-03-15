package com.feedss.user.service;

import java.util.List;

import com.feedss.user.entity.Role.RoleType;
import com.feedss.user.entity.UserRole;

/**
 * 用户角色
 * @author 张杰
 *
 */
public interface UserRoleService {


	UserRole save(String userId, String roleId);

	UserRole find(String userId, String roleId);
	
	int removeUserRole(RoleType roleType, String userId);
	
	UserRole addUserRole(RoleType roleType, String userId);
	
	List<String> getUserIds(RoleType roleType);
	
	boolean isRole(RoleType roleType, String userId);
	
}
