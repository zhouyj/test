package com.feedss.contact.controller.model;

import lombok.Data;

@Data
public class Notification {
	private String[] toAccount;
	
	private String title; //通知标题，ios中无用
	private String content;//通知内容，限长：
	private String ext;
}
