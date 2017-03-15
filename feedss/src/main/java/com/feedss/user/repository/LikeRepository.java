package com.feedss.user.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.Like;

/**
 * 点赞
 * @author Administrator
 *
 */
public interface LikeRepository extends BaseRepository<Like>{

	/**
	 * 获取赞总数
	 * @param objct
	 * @param objectId
	 * @return
	 */
	@Query("select sum(l.count)  from Like l "
			+ "where l.object=:object and l.objectId=:objectId")
	long getCount(@Param("object")String object,@Param("objectId")String objectId);

	/**
	 * 验证用户是否点赞
	 * @param object
	 * @param objectId
	 * @param userId
	 * @return
	 */
	@Query("select count(*) from  Like l"
			+ " where l.object=:object and l.objectId=:objectId and l.userId=:userId and type=:type")
	int exist(@Param("object")String object,@Param("objectId")String objectId,
			@Param("userId")String userId,@Param("type")String type);
	
	
	
	
	
}
