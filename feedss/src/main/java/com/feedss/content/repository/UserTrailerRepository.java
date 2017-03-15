package com.feedss.content.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.feedss.content.entity.UserTrailer;

/**
 * 用户预约<br>
 * @author wangjingqing
 * @date 2016-08-01
 */
public interface UserTrailerRepository extends JpaRepository<UserTrailer, String> {
	
	/**
	 * 删除用户预告<br>
	 * @param userId
	 * @param trailerId
	 * @return
	 */
	@Modifying
	@Query("update UserTrailer ut set ut.isDelete= true where ut.userId=?1 and ut.trailerId=?2")
	public Integer deleteUserTrailer(String userId,String trailerId);
	
	/**
	 * 查询用户预告<br>
	 * @param userId
	 * @param trailerId
	 * @return
	 */
	@Query("select ut from  UserTrailer ut where ut.isDelete=false and ut.userId=?1 and ut.trailerId=?2")
	public UserTrailer selectByUserIdAndTrailerId(String userId,String trailerId);
	
	/**
	 * 批量查询用户预约信息<br>
	 * @param userId
	 * @param trailerIds
	 * @return
	 */
	@Query("select ut from  UserTrailer ut where ut.isDelete=false and ut.userId=?1 and ut.trailerId  in ?2")
	public List<UserTrailer> selectByUserIdAndTrailerId(String userId,List<String> trailerIds);
	
	@Query("select ut from  UserTrailer ut where ut.isDelete=false and ut.trailerId =?1")
	public List<UserTrailer> selectByTrailerId(String trailerId);
}
