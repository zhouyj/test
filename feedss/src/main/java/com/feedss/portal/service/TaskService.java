package com.feedss.portal.service;

import java.util.List;

import com.feedss.base.JsonData;
import com.feedss.base.Row;
import com.feedss.portal.dto.TaskDTO;
import com.feedss.portal.entity.Task;
import com.feedss.portal.entity.TaskAccept;

public interface TaskService {

    public Task add(String userId, String content, String streamId);

    public Task get(String taskId);

    public TaskAccept getAccept(String taskId);

    public Task getAccept(String userId, String taskId);

    public JsonData rob(String userId, String taskId);

    public boolean applyFinish(String userId, String taskId);

    public boolean confirmFinish(String userId, String taskId);

    public boolean reject(String userId, String taskId, String content);
    
    public List<TaskDTO> getPublishTaskList(String userId, String streamId);
    
    public int getRobCount(String userId, String streamId);
    
    /**
     * 直播间中的任务
     * @param streamId
     * @return
     */
    public List<TaskDTO> getPublishTaskList(String streamId, int status);

    public List<TaskDTO> getAcceptTaskList(String userId, String streamId);

    public boolean del(String taskId);

    public boolean delAccept(String userId, String taskId);

    public Row getTaskCount(String userId);
    
    public int getPublishTaskCount(String userId);
    
    public int getAcceptTaskCount(String userId);
    
    public Row getUser(String userId);
}
