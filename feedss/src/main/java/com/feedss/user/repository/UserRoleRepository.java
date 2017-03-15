package com.feedss.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.UserRole;

/**
 * 用户角色
 * @author 张杰
 *
 */
public interface UserRoleRepository extends BaseRepository<UserRole> {

	
	@Modifying
	@Query("update UserRole as ur set status=:status where userId=:userId and roleId=:roleId")
	public int updateStatus(@Param("status")int status,@Param("userId")String userId
			,@Param("roleId")String roleId);
	
	@Query("select ur from UserRole as ur where ur.userId=:userId and ur.roleId=:roleId and ur.status=0")
	public UserRole find(@Param("userId")String userId,@Param("roleId")String roleId);
	
	@Modifying
	@Query("delete from UserRole as ur where userId=:userId and roleId=:roleId")
	public int delete(@Param("userId")String userId, @Param("roleId")String roleId);
	
	@Query("select userId from UserRole as ur where ur.roleId=:roleId and ur.status=0")
	public List<String> findRoleId(@Param("roleId")String roleId);
	
}
