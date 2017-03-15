package com.feedss.user.entity;

import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;
/**
 * 
 * @author 张杰
 *
 */
@Entity
@Data
public class Statistics extends BaseEntity{

	private String uid;//用户id
	
	private String objcet;//对象
	
	private String objcetId;//对象id
	
	private String action;//动作
	
	private String info;//动作的信息
	
}
