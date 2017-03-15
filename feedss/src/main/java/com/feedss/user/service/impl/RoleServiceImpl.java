package com.feedss.user.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feedss.user.entity.Role;
import com.feedss.user.entity.RolePermission;
import com.feedss.user.repository.PermissionRepository;
import com.feedss.user.repository.RolePermissionRepository;
import com.feedss.user.repository.RoleRepository;
import com.feedss.user.repository.UserRoleRepository;
import com.feedss.user.service.RoleService;

/**
 * 角色
 * @author 张杰
 *
 */
@Service
public class RoleServiceImpl implements RoleService{

	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	UserRoleRepository userRoleRepository;
	
	@Autowired
	RolePermissionRepository rolePermissionRepository;
	
	@Autowired
	PermissionRepository permissionRepository;
	
	@Override
	public List<Role> roleList(){
		return roleRepository.findAll();
	}
	@Override
	public Role getByUuid(String roleId){
		return roleRepository.findOne(roleId);
	}
	@Override
	public Role getByCode(String roleCode) {
		return roleRepository.findByCode(roleCode);
	}
	@Override
	public List<Map<String, String>> getRoles(String userId) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		List<Role> roles = roleRepository.findRolesByUserId(userId);
		if(roles!=null && !roles.isEmpty()){
			for(Role role:roles){
				Map<String, String> map = new HashMap<String, String>();
				map.put("name", role.getName());
				map.put("code", role.getCode());
			}
		}
		return result;
	}
	@Override
	public List<String> getPermissions(String userId) {
		List<String> permissions = new ArrayList<>();
		List<Role> roles = roleRepository.findRolesByUserId(userId);
		if(roles!=null && !roles.isEmpty()){
			for(Role r:roles){
				List<RolePermission> rolePermissions = rolePermissionRepository.find(r.getUuid());
				if(rolePermissions==null || rolePermissions.isEmpty()) continue;
				for(RolePermission rp:rolePermissions){
					if(rp.getPermission()!=null){
						permissions.add(rp.getPermission().getName());
					}
				}
			}
		}
		return permissions;
	}
	
}
