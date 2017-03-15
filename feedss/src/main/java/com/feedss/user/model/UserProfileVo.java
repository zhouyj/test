package com.feedss.user.model;

import java.io.Serializable;
import java.util.Map;


import lombok.Data;

@Data
public class UserProfileVo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9118116612701316685L;
	private String nickname;
	private String avatar;
	private int gender;
	private int level;
	private int age;
	private String birthdate;
	
	private Map<String, String> ext;
	
}
