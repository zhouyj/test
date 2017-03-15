package com.feedss.manage.model;

import lombok.Data;

@Data
public class Profile{
	private String nickname;
	private String avatar;
	private int gender;
	private int level;
	private Integer age;
	private String birthdate;
}