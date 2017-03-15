package com.feedss.contact.qcloud.domain;

import java.util.List;

import com.feedss.contact.qcloud.domain.QMessageTransfer.QMessageElement;

import lombok.Data;

@Data
public class GroupMsgRequestBody {

	private String GroupId;
	
	private int Random;
	
	private List<QMessageElement> MsgBody;
	
	private String From_Account;
}
