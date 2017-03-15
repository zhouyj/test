package com.feedss.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.feedss.portal.entity.Task;

/**
 * Created by shenbingtao on 2016/7/23.
 */
@Repository
public interface TaskRepository extends CrudRepository<Task, String> {
    
    @Query(value = "select a.* from task a where a.creator = ?1 and a.type != 1 order by a.created desc", nativeQuery = true)
    public List<Task> getList(String userId);

    @Query(value = "select b.* from task_accept a, task b where a.task_id=b.uuid and a.creator = ?1 and a.type != 1 order by a.created desc", nativeQuery = true)
    public List<Task> getAcceptList(String userId);

    @Query(value = "select a.* from task a where a.creator = ?1 and a.stream_id = ?2 and a.type != 1 order by a.created desc", nativeQuery = true)
    public List<Task> getListByStream(String userId, String streamId);
    
    @Query(value = "select a.* from task a where a.stream_id = ?1 and a.type != 1 and a.status=?2 order by a.created desc", nativeQuery = true)
    public List<Task> getListByStream(String streamId, int status);

    @Query(value = "select b.* from task_accept a, task b where a.task_id=b.uuid and a.creator = ?1 and b.stream_id = ?2 and a.type != 1 order by a.created desc", nativeQuery = true)
    public List<Task> getAcceptListByStream(String userId, String streamId);

    @Query(value = "select b.* from task_accept a, task b where a.task_id=b.uuid and a.creator = ?1 and a.task_id = ?2 and a.type != 1 order by a.created desc", nativeQuery = true)
    public Task getAccept(String userId, String taskId);
    
    @Query(value = "select count(1) from task a where a.creator = ?1 and a.type != 1 and a.status= ?2", nativeQuery = true)
    public int count(String userId, int status);

    @Query(value = "select count(1) from task a where a.creator = ?1 and a.stream_id = ?2 and a.type != 1 and a.status= ?3", nativeQuery = true)
    public int countByStream(String userId, String streamId, int status);
}
