package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 角色
 * 0001:普通用户，0002:vip，0003:admin，0004:企业认证
 * @author tangjun
 *
 */
@Entity
@Data
public class Role extends BaseEntity {

	public enum RoleType{
	    USER("0000", "user"),
	    V("0001", "V"),
		ADMIN("0002", "admin"),
		HOST("0003", "host"),
		API("0004", "api"),
	    ;
	    private String name;
		
		private String code;
		
		private RoleType(String code, String name){
			this.code = code;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}
		
	}
	
	@Column(length = 36, nullable = true)
	private String code;// 编码
}
