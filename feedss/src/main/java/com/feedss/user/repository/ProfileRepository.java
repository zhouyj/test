package com.feedss.user.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.Profile;
import com.feedss.user.entity.User;

public interface ProfileRepository extends BaseRepository<Profile>{

	@Query("select p from Profile p where p.userId=:userId")
	public Profile findByUserId(@Param("userId")String userId);
	
	
	@Modifying
	@Query("update Profile p set p.level=:level,p.integral=:integral where p.userId=:userId")
	public int updateLevel(@Param("userId")String userId,@Param("level")int level
			,@Param("integral")long integral);
}