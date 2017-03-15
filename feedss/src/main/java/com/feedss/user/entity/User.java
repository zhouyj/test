package com.feedss.user.entity;

import javax.persistence.*;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 用户
 * 
 * @author tangjun
 *
 */
//@Table(name="t_userinfo")
@Entity
@Data
public class User extends BaseEntity {
	
	public enum UserStatus{
		NORMAL, DISABLED
	}
	
	@Column(length = 36, nullable = true)
	private String username;// 用户名

	@Column(length = 20, nullable = true)
	private String mobile;// 手机号

	@Column(length = 20, nullable = true)
	private String email;// 电子邮件

	@Column(length = 36, nullable = true)
	private String password;// 密码

	@Column(length = 36, nullable = true)
	private String sessionId;// session Id

	@Column(length = 36, nullable = true)
	private String token;// token

	@Column(length = 36, nullable = true)
	private String thirdpartyName;
	
	@Column(length = 36, nullable = true)
	private String thirdpartyId;
	
	@Column(length = 100, nullable = true)
	private String thirdpartyToken;

	@Column(length = 100, nullable = true)
	private String mallUserToken;

	@OneToOne
	@JoinColumn(name = "profileid", foreignKey = @ForeignKey(name = "none"))
	Profile profile;
}
