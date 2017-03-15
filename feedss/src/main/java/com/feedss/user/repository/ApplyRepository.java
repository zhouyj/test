package com.feedss.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.Apply;
/**
 * 申请认证
 * @author 张杰
 *
 */
public interface ApplyRepository extends BaseRepository<Apply>{

	String table=Apply.class.getSimpleName();
	
	@Modifying
	@Query("update Apply a set a.status=:status where a.uuid=:uuid")
	public int updateStatus(@Param("uuid")String uuid,@Param("status")int status);
	
	/**
	 * 通过的申请 变为-2
	 * @param userId
	 * @param status
	 * @return
	 */
	@Modifying
	@Query("update Apply a set a.status=-2 where a.userId=:userId and status=1")
	public int updateStatusBy1(@Param("userId")String userId);
	
	
	@Query("Select a from Apply a where a.userId=:userId and a.action=:action "
			+ " and a.object=:object and a.objectId=:objectId and status>=0 "
			+ " order by a.created desc ")
	public List<Apply> ApplyStatus(@Param("action")String action,@Param("object")String object,
			@Param("userId")String userId,@Param("objectId")String objectId );
	
}
