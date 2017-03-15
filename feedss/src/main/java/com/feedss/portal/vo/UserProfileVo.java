package com.feedss.portal.vo;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

/**
 * 用户个人信息
 * @author Looly
 *
 */
@Data
public class UserProfileVo {
	
	private String uuid;
	private String token;
	
	private String nickname;
	private String avatar;
	private int gender;
	
	//生日，在服务端拼接
	private String birthdateYear;
	private String birthdateMonth;
	private String birthdateDay;
	
	//扩展
	private String province;
	private String city;
	private String district;
	
	private String school;
	
	private String grade;
	
	
	private String email;
	private String qq;
	private String weibo;
	private String parent;
	private String contact;
	private String address;
	private String hobbies;
	
	public String getBirthdate(){
		StringBuilder dateBuilder = new StringBuilder();
		if(StringUtils.isAnyBlank(this.birthdateYear, this.birthdateMonth, this.birthdateDay)){
			return null;
		}
		return dateBuilder
				.append(this.birthdateYear).append("-")
				.append(this.birthdateMonth).append("-")
				.append(this.birthdateDay).toString();
	}
}
