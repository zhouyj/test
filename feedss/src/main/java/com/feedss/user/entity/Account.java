package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 用户虚拟货币账户
 * 
 * @author tangjun
 *
 */
@Entity
@Data
public class Account extends BaseEntity {
	
	//private String type; baseEntity中以定义, 为空时默认是虚拟币
	public enum AccountType{
		VIRTURECOIN, //虚拟币
		POINTS, //积分
		CASH; //现金
	}
	
	@Column(length = 36, nullable = false)
	private String userId;// 用户userId

	private int balance;// 余额

}
