package com.feedss.user.model;

import java.util.Date;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import lombok.Data;

/**
 * 审核
 * @author 张杰
 *
 */
@Data
public class ApplyVo {
	
	String uuid;
	String userId;
	Date created;
	String mobile;
	int gender;
	int level;
	String avatar;
	Date birthdate;
	String nickname;
	String roleName;
	int status;
	
	int age;
	

	/**
	 * 
	 * @param birth
	 * @return
	 */
	public int getAge() {
	    if (birthdate == null) {
	        return 0;
	    }
	    //时间解析
	    LocalDate birthday = LocalDate.fromDateFields(birthdate);
	    LocalDate now = new LocalDate();
	    Years age = Years.yearsBetween(birthday, now);
	    return age.getYears();
	}
}
