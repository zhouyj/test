package com.feedss.portal.entity;

import javax.persistence.Entity;
import javax.persistence.Transient;

import com.feedss.base.BaseEntity;
import com.feedss.user.model.UserVo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
@Entity
public class Interaction extends BaseEntity{

	/**
	 * 状态
	 * @author zhouyujuan
	 *
	 */
	public enum InteractionStatus{
		PUBLISHED, CONFIRMED, REJECTED
	}
	
	public enum InteractionType{
		BonusTask("任务")  // 观众发起的任务
		, Bid("投标")   // 投标
		;
		private String msg;
		
		private InteractionType(String msg){
			this.msg = msg;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}
	}
	
	private String streamId;
	private String receiver;
	private int price;
	private boolean isCreatorDel; //发起方删除
	private boolean isReceiverDel;  //接受方删除
	
	@Transient
	private UserVo createUserInfo;
	@Transient
	private UserVo receiveUserInfo;
}
