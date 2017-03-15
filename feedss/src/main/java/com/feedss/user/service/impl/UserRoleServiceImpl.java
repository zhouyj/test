package com.feedss.user.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feedss.user.entity.Role;
import com.feedss.user.entity.Role.RoleType;
import com.feedss.user.entity.UserRole;
import com.feedss.user.repository.ApplyRepository;
import com.feedss.user.repository.RoleRepository;
import com.feedss.user.repository.UserRoleRepository;
import com.feedss.user.service.UserRoleService;
/**
 * 
 * @author 张杰
 *
 */
@Service
public class UserRoleServiceImpl implements UserRoleService{

	@Autowired
	UserRoleRepository userRoleRepository;
	
	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	ApplyRepository applyRepository;
	
	@Override
	public UserRole save(String userId,String roleId){
		UserRole userRole=new UserRole();
		userRole.setCreated(DateTime.now().toDate());
		userRole.setUserId(userId);;
		userRole.setRoleId(roleId);
		return userRoleRepository.save(userRole);
	}
	
//	@Transactional
//	@Override
//	public int updateVStatus(int status,String userId){
//		Role role=roleRepository.findByCode(RoleType.V.getCode());
//		if(status==1){
//			applyRepository.updateStatusBy1(userId);
//		}
//		return userRoleRepository.updateStatus(status, userId, role.getUuid());
//	}
	
	@Transactional
	@Override
	public int removeUserRole(RoleType roleType,String userId){
		Role role=roleRepository.findByCode(roleType.getCode());
		if(role==null) return 0;
		if(roleType==RoleType.V){
			applyRepository.updateStatusBy1(userId);
		}
		return userRoleRepository.delete(userId, role.getUuid());
	}
	
	@Override
	public UserRole find(String userId,String roleId){
		return userRoleRepository.find(userId, roleId);
	}

	@Override
	public UserRole addUserRole(RoleType roleType, String userId) {
		Role role=roleRepository.findByCode(roleType.getCode());
		if(role==null) return null;
		UserRole userRole = userRoleRepository.find(userId, role.getUuid());
		if(userRole==null){
			userRole = save(userId, role.getUuid());
		}
		return userRole;
	}

	@Override
	public List<String> getUserIds(RoleType roleType) {
		Role role=roleRepository.findByCode(roleType.getCode());
		if(role==null) return null;
		return userRoleRepository.findRoleId(role.getUuid());
	}

	@Override
	public boolean isRole(RoleType roleType, String userId) {
		Role role=roleRepository.findByCode(roleType.getCode());
		if(role==null) return false;
		UserRole userRole = userRoleRepository.find(userId, role.getUuid());
		return userRole==null?false:true;
	}
}
