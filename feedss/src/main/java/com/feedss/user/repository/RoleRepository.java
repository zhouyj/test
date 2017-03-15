package com.feedss.user.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.Role;

import java.util.List;

/**
 * Created by qinqiang on 2016/8/1.
 */
public interface RoleRepository extends BaseRepository<Role>{

	@Query("select r from Role r where r.uuid in (select ur.roleId from UserRole ur where ur.userId=:userId and status=0)")
	public List<Role> findRolesByUserId(@Param("userId")String userId);
	
	@Query("select r from Role r where code=:code")
	public Role findByCode(@Param("code")String code);
}
