package com.feedss.contact.qcloud.domain;

import java.util.List;
import java.util.Map;

import com.feedss.contact.qcloud.domain.QMessageTransfer.QMessageElement;

import lombok.Data;

@Data
public class PushMessageRequestBody {

	private String From_Account;
	
	private int MsgRandom;
	
	private int MsgLifeTime;
	
	private QCondition Condition;
	
	private List<QMessageElement> MsgBody;
	
	@Data
	public class QCondition{
		private Map<String, String> AttrsAnd;
		private Map<String, String> AttrsOr;
		private String[] TagsAnd;
		private String[] TagsOr;
	}
}
