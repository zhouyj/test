package com.feedss.portal.controller.domain;

import lombok.Data;

/**
 * Created by shenbingtao on 2016/8/7.
 */
@Data
public class TaskParam {

    private String userId;

    private String taskId;

    private String description;//决绝理由或者任务描述

    private Integer pageNo;

    private Integer pagSize;
    
    private String streamId;

    private String taskStatus;
}
