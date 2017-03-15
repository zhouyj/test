package com.feedss.portal.dto;

import lombok.Data;

import java.util.List;

import com.feedss.portal.entity.Task;
import com.feedss.portal.entity.TaskReject;

/**
 * Created by shenbingtao on 2016/8/21.
 */
@Data
public class TaskDTO {

    public TaskDTO(){}

    public TaskDTO(Task task){
        taskId = task.getUuid();
        description = task.getDescription();
        creator = task.getCreator();
        status = task.getStatus();
    }

    private int status;

    private String taskId;

    private String description;

    private String creator;

    private List<TaskReject> rejectList;

    private String nickname = "";

    private String avatar = "";

    private String statusDescription;//状态描述

    public static String getStatusDescription(int status, String nickname){
        if(status == Task.TaskStatus.PUBLIC.ordinal()){
            return "暂时没有人抢到";
        }else if(status == Task.TaskStatus.ACCEPT.ordinal()){
            return nickname + "执行中";
        }else if(status == Task.TaskStatus.REPLY_FINISH.ordinal()){
            return nickname + "已确认完成任务";
        }else if(status == Task.TaskStatus.FINISH.ordinal()){
            return nickname + "成功完成任务";
        }else if(status == Task.TaskStatus.REJECT.ordinal()){
            return nickname + "执行中";
        }

        return "";
    }

}
