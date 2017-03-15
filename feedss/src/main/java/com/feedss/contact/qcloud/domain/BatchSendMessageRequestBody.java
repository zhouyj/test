package com.feedss.contact.qcloud.domain;

import java.util.List;

import com.feedss.contact.qcloud.domain.QMessageTransfer.QMessageElement;

import lombok.Data;

@Data
public class BatchSendMessageRequestBody {

	private String From_Account;
	
	private String[] To_Account;
	
	private int MsgRandom;
	
	private List<QMessageElement> MsgBody;
}
