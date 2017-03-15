package com.feedss.contact.entity;

import lombok.Data;
/**
 * 群消息
 * @author zhouyujuan
 *
 */
@Data
public class GroupMessage extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5425561887405447841L;
	private String groupId;
}
