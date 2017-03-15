package com.feedss.portal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "task")
public class Task extends BaseEntity {

	public enum TaskStatus {
		PUBLIC, // 主播发布
		ACCEPT, // 网友接收
		REPLY_FINISH, // 网友申请完成
		FINISH, // 主播操作任务完成
		REJECT // 主播驳回
	}

	public enum TaskType {
		NORMAL, // 正常
		DEL // 删除
	}

	// 用到的字段
	// creator 任务发布者
	// decription 任务描述，最多100字
	@Column(length = 36, nullable = false, updatable = false)
	private String streamId;// 任务关联

}
