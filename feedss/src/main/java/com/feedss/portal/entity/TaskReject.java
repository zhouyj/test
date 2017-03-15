package com.feedss.portal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务日志 
 * 
 * @author Created by shenbingtao on 2016/7/23.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "task_reject")
public class TaskReject extends BaseEntity {

	@Column(length = 36, nullable = false)
	private String taskId;

	// 使用到的字段
	// description 驳回理由
	// created 驳回时间

}
