package com.feedss.contact.qcloud.domain;

import lombok.Data;

@Data
public class ActionResponse<T> {

	private String ActionStatus;
	
	private int ErrorCode;
	
	private String ErrorInfo;
	
	private T t;
}
