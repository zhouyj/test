package com.feedss.user.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.UserRelation;
import com.feedss.user.entity.UserRelation.RelationType;

/**
 * 用户关系
 * @author 张杰
 *
 */
public interface UserRelationRepository extends BaseRepository<UserRelation>{

	
	@Query(value="select to_user_id as userId ,count(*) as num from user_relation ur "
			+ "where ur.status=0 and ur.created>:created "
			+ " group by to_user_id "
			+ " order by num desc "
			+ " limit :start,:size ",nativeQuery=true)
	public List xRank(@Param("created")String created
			,@Param("start")int start,@Param("size")int size);
	
	@Query(value="select count(1) from( select count(to_user_id) from user_relation ur "
			+ "where ur.status=0 and ur.created>:created "
			+ " group by to_user_id) as a ",nativeQuery=true)
	public Integer xRankCount(@Param("created")String created);
	
	
	/**
	 * 根据 关系主体 查询 
	 * @param userId
	 * @param type {@link RelationType}
	 * @return
	 */
	@Query("select ur from UserRelation ur where fromUserId=:userId and type=:type and status=0")
	List<UserRelation> findByFromUserId(@Param("userId")String userId,
			@Param("type")String type);
	
	/**
	 * 根据 关系主体 查询 
	 * @param userId
	 * @param type {@link RelationType}
	 * @return
	 */
	@Query("select ur.toUserId from UserRelation ur where fromUserId=:userId and type=:type and status=0")
	List<String> findToUserIdByFromUserId(@Param("userId")String userId,
			@Param("type")String type);
	
	/**
	 * 根据 关系主体 查询总数
	 * @param userId
	 * @param type {@link RelationType}
	 * @return
	 */
	@Query("select count(*) from UserRelation ur where fromUserId=:userId and type=:type and status=0")
	long getCountByFromUserId(@Param("userId")String userId,
			@Param("type")String type);
	
	/**
	 * 更多 我关注的人 
	 * @param userId
	 * @param type
	 * @param cursorId 光标id
	 * @param time
	 * @param size
	 * @return
	 */
	@Query("select ur from UserRelation ur "
			+ "where fromUserId=:userId and status=0 and type=:type "
			+ "and (ur.created<:time or (ur.created=:time and ur.uuid>:uuid))"
			/*			+ " group by toUserId "    */
			+ " order by ur.created desc ,ur.uuid "
			)
	Page<UserRelation> moreByFromUserId(@Param("userId")String userId,
			@Param("type")String type,@Param("uuid")String cursorId,@Param("time")Date time,
			Pageable pageable);
	
	
	/**
	 * 更多 关注我的人 
	 * @param userId
	 * @param type
	 * @param cursorId 光标id
	 * @param time
	 * @param size
	 * @return
	 */
	@Query("select ur from UserRelation ur "
			+ "where toUserId=:userId and status=0 and type=:type  "
			+ "and (ur.created<:time or (ur.created=:time and ur.uuid>:uuid))"
	/*		+ " group by fromUserId "*/
			+ " order by ur.created desc,ur.uuid "
			)
	Page<UserRelation> moreByToUserId(@Param("userId")String userId,
			@Param("type")String type,@Param("uuid")String cursorId,@Param("time")Date time,
			Pageable pageable);
	
	/**
	 * 根据 关系客体 查询 
	 * @param userId
	 * @param type {@link RelationType}
	 * @return
	 */
	@Query("select ur from UserRelation ur where toUserId=:userId and type=:type and status=0")
	List<UserRelation> findByToUserId(@Param("userId")String userId,
			@Param("type")String trpe);
	
	/**
	 * 根据 关系客体 查询 
	 * @param userId
	 * @param type {@link RelationType}
	 * @return
	 */
	@Query("select ur.fromUserId from UserRelation ur where toUserId=:userId and type=:type and status=0")
	List<String> findFromUserIdByToUserId(@Param("userId")String userId,
			@Param("type")String trpe);
	
	
	/**
	 * 根据 关系客体 查询总数 
	 * @param userId
	 * @param type {@link RelationType}
	 * @return
	 */
	@Query("select count(*) from UserRelation ur where toUserId=:userId and type=:type and status=0")
	long getCountByToUserId(@Param("userId")String userId,
			@Param("type")String trpe);
	
	/**
	 * 
	 * @param userId
	 * @param toUserId
	 * @param type
	 * @return
	 */
	@Query("select ur from UserRelation ur where toUserId=:toUserId and fromUserId=:fromUserId and type=:type and status=0")
	UserRelation find(@Param("fromUserId")String fromUserId,@Param("toUserId")String toUserId,
			@Param("type")String type);
	
	/**
	 * 
	 * @param uuid
	 * @param status  0:可用  1:不可用
	 * @return
	 */
	@Modifying
	@Query("update UserRelation ur set ur.status=:status where ur.uuid=:uuid")
	int updateStatus(@Param("uuid")String uuid,@Param("status")int status);
	
	
	/**
	 * 
	 * @param uuid
	 * @param status  0:可用  1:不可用
	 * @return
	 */
	@Modifying
	@Query("update UserRelation ur set ur.status=:status where ur.toUserId=:toUserId  and fromUserId=:fromUserId and type=:type and status=0 ")
	int updateStatus(@Param("fromUserId")String fromUserId,@Param("toUserId")String toUserId,
			@Param("type")String type,@Param("status")int status);
	
	@Modifying
	@Query("delete from UserRelation ur where ur.toUserId=:toUserId and fromUserId=:fromUserId and type=:type")
	int delete(@Param("fromUserId")String fromUserId,@Param("toUserId")String toUserId,
			@Param("type")String type);
}
