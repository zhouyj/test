package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.feedss.base.BaseEntity;

import lombok.Data;


/**
 * 群组
 * 
 * @author tangjun
 *
 */
@Table(name="t_group")
@Entity
@Data
public class Group extends BaseEntity {
	@Column(length = 36, nullable = true)
	private String owner;// 群主userID
}
