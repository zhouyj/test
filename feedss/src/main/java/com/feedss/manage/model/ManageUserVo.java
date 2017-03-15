package com.feedss.manage.model;

import java.util.Date;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import lombok.Data;

@Data
public class ManageUserVo {

	String uuid;
	Date created;
	String mobile;
	Integer gender;
	Integer level;
	String avatar;
	Date birthdate;
	String nickname;
	String ruuid;//加V uuid
	String rmuuid;//管理员 uuid
	String rhuuid;//主持人 uuid
	Integer status;
	Integer age;
//	
//	public void init(JSONObject json){
//		this.uuid=json.getString("uuid");
//		this.created=json.getLong("created");
//		this.mobile=json.getString("mobile");
//		this.gender=json.getInteger("gender");
//		this.level=json.getInteger("level");
//		this.avatar=json.getString("avatar");
//		this.birthdate=json.getLong("birthdate");
//		this.nickname=json.getString("nickname");
//		this.ruuid=json.getString("ruuid");
//		this.status=json.getInteger("status");
//		this.rmuuid=json.getString("rmuuid");
//		this.rhuuid=json.getString("rhuuid");
//		this.age=getAgeFromBirth((new DateTime(birthdate)).toDate());
//	}
//	
	/**
	 * 
	 * @param birth
	 * @return
	 */
	public static int getAgeFromBirth(Date date) {
	    if (date == null) {
	        return 0;
	    }
	    //时间解析
	    LocalDate birthday = LocalDate.fromDateFields(date);
	    LocalDate now = new LocalDate();
	    Years age = Years.yearsBetween(birthday, now);
	    return age.getYears();
	}
	
	
}