package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 申请表
 * @author zhangjie
 *
 */
@Data
@Entity
public class Apply extends BaseEntity {

	public enum ApplyAction {
		AUTH 
	}
	
	
	@Column(length = 36, nullable = false)
	private String action;//申请动作  例如:auth(认证)
	
	@Column(length = 36, nullable = false)
	private String userId;//发起用户
	
	@Column(length = 36, nullable = false)
	private String object;//          例如:role(角色表)
	
	@Column(length = 36, nullable = true)
	private String objectId;//        例如:roleid(角色表的id【vip】【超级vip】)
	
	@Column(length = 500, nullable = true)
	private String reason;
	
	private int del;//1:删除
	//status  0:初始化  1:通过  -1 不通过  -2:通过后消除V
	
}
