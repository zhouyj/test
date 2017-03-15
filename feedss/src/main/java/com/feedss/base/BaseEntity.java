package com.feedss.base;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@MappedSuperclass
@Data
@JsonInclude(Include.NON_NULL) 
public class BaseEntity {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = " int(20) not null AUTO_INCREMENT, key(id) ", unique = true, updatable = false, insertable=false)
	@JsonIgnore
	private Long id;// 数据库自增id，不作为业务对象唯一标识

	@Id
	@Column(length = 36, nullable = false)
	private String uuid;// 业务对象全局唯一id

	@Column(length = 36, nullable = true)
	private String name;// 名称或标题

	@Column(length = 1024, nullable = true)
	private String description;// 描述或正文

	@Column(length = 128, nullable = true)
	private String remark;// 备注

	@Column(length = 128, nullable = true)
	private String tags;// 标签，逗号分隔

	@Column(length = 36)
	private String type;// 类型

	@Column(nullable = false)
	private int status;// 状态

	@Column(nullable = true)
	private int rank;// 评分，影响排序
	
	@Column(nullable = true)
	private Integer sort;// 排序

	@Column(length = 1024, nullable = true)
	private String extAttr;// 扩展字段，JSON格式{"key":"value"}

	@Column(length = 36, nullable = true)
	private String creator;// 创建者userId

	@Column(columnDefinition = " datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ")
	private Date created;// 创建时间，数据库取当前时间

	@Column(length = 36, nullable = true)
	private String updater;// 更新者userId

	@Column(columnDefinition = " datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ")
	private Date updated;// 更新时间，数据库自动更新为当前时间，索引更新依赖字段

	public BaseEntity() {
		this.uuid = UUID.randomUUID().toString();
		this.created = new Date();
		this.updated = new Date();
	}
}
