package com.feedss.user.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 用户属性
 * 
 * @author tangjun
 *
 */
@Entity
@Data
public class Profile extends BaseEntity {
	@Column(length = 36, nullable = false)
	private String userId;// 用户userId
	// @OneToOne
	// User user;

	@Column(length = 36, nullable = false)
	private String nickname;// 昵称

	private Date birthdate;// 出生日期

	@Column(length = 300, nullable = true)
	private String avatar;// 头像URI

	private int gender;// 性别

	private int locationCode;// 地区编码

	private int level;// 等级

	private long integral;
}
