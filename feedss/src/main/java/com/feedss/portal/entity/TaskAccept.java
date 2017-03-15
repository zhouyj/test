package com.feedss.portal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 网友抢到的任务
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Entity
@Table(name = "task_accept")
public class TaskAccept extends BaseEntity {

	@Column(length = 36, nullable = false)
	private String taskId;

}
