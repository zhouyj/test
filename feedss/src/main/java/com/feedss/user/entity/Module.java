package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 模块
 * 目前控制 空间的模块
 * @author 张杰
 *
 */
@Data
@Entity
public class Module extends BaseEntity {

	public enum ExecuteType{
		Native,H5
	}
	
	public enum ModuleType{
		Base,Extend
	}
	
	
	
	@Column(length = 500, nullable = true)
	private String url;
	
	@Column(length = 500, nullable = true)
	private String ioc;
	
	@Column(length = 255, nullable = true)
	private String code;

	private String executeType;//执行类型
}
