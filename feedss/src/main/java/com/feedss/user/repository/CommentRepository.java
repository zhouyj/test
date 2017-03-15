package com.feedss.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.Comment;

import java.util.Date;
import java.util.List;

/**
 * Created by qinqiang on 2016/8/6.
 */
public interface CommentRepository  extends BaseRepository<Comment>{
	@Query("select p from Comment p where p.objectId=:objectId ")
	public Page<Comment> findNewestComments(@Param("objectId")String objectId,Pageable pageable);

	@Query("select p from Comment p where p.objectId=:objectId and ( p.created>:startDate or (p.created=:startDate and p.uuid>:uuid) ) ")
	public Page<Comment> findPreComments(@Param("objectId")String objectId, @Param("startDate")Date startDate, @Param("uuid")String uuid, Pageable pageable);

	@Query("select p from Comment p where p.objectId=:objectId and ( p.created<:startDate or (p.created=:startDate and p.uuid<:uuid) ) ")
	public Page<Comment> findNextomments(@Param("objectId")String objectId, @Param("startDate")Date startDate,@Param("uuid")String uuid,Pageable pageable);

}
