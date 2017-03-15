package com.feedss.user.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.Favorite;

/**
 * 收藏
 * @author 张杰
 *
 */
public interface FavoriteRepository extends BaseRepository<Favorite> {

	@Query("select f from Favorite f where f.userId=:userId and status=0"
			+ "and (f.created>:time or ((f.created=:time and f.uuid>:uuid)))"
			+ " order by f.created desc ,f.uuid ")
	Page<Favorite> pageByUserId(@Param("userId")String userId,
			@Param("uuid")String cursorId,@Param("time")Date time,
			Pageable pageable);
	
	@Query("select f from Favorite f where f.userId=:userId and status=0 and type=:type")
	List<Favorite> getByUserId(@Param("userId")String userId, @Param("type")String type);
	
	@Modifying
	@Query("update Favorite f set f.status=1 where f.uuid=:uuid")
	int updateStatus(@Param("uuid")String uuid);
	
	@Modifying
	@Query("update Favorite f set f.status=1 where f.userId=:userId "
			+ "and f.object=:object and f.objectId=:objectId and f.type=:type")
	int updateStatus(@Param("userId")String userId,@Param("object")String object,
			@Param("objectId")String objectId, @Param("type")String type);
	
	@Query("select count(*) from Favorite f where f.userId=:userId "
			+ "and f.object=:object and f.objectId=:objectId and f.status=0 and type=:type")
	int isExist(@Param("userId")String userId,@Param("object")String object,
			@Param("objectId")String objectId, @Param("type")String type);
	
	@Query("select f from Favorite f where f.userId=:userId and f.object=:object"
			+ " and f.objectId=:objectId and f.status=0 and f.type=:type")
	Favorite findFavorite(@Param("userId")String userId,@Param("object")String object,
			@Param("objectId")String objectId, @Param("type")String type);
	
}
