package com.feedss.portal.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.feedss.portal.entity.TaskReject;

import java.util.List;

/**
 * Created by shenbingtao on 2016/7/23.
 */
@Repository
public interface TaskRejectRepository extends CrudRepository<TaskReject, String> {

    @Query(value = "select a.* from task_reject a where a.task_id = ?1 order by a.created desc", nativeQuery = true)
    public List<TaskReject> getList(String taskId);

}
