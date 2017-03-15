package com.feedss.portal.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.feedss.portal.entity.TaskAccept;

/**
 * Created by shenbingtao on 2016/7/23.
 */
@Repository
public interface TaskAcceptRepository extends CrudRepository<TaskAccept, Long> {

	@Query("select a from TaskAccept a where a.taskId = ?1 and a.creator= ?2")
	TaskAccept findByTaskIdAndCreator(String taskId, String userId);

	@Query("select a from TaskAccept a where a.taskId = ?1")
	TaskAccept findByTaskId(String taskId);

}
