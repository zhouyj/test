package com.feedss.user.entity;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 账户交易
 * 
 * @author tangjun
 *
 */
@Entity
@Data
//@AllArgsConstructor
@NoArgsConstructor
public class AccountTransaction extends BaseEntity {
	
	public static Date UnExpiredTime = null;
	static{
		Calendar calendar = Calendar.getInstance();
		calendar.set(2999, 1, 1);
		UnExpiredTime = calendar.getTime();
	}
	
	public enum AccoutTransactionSourceType {
		buyGift("购买礼物"), // 购买礼物
		charge("微信充值") // 充值
		,systemPresentViewArticle("系统赠送") //看文章系统赠送
		,systemPresentLogin("系统赠送") //登录系统赠送
		,systemPresentProfile("系统赠送") //资料填写
		,interactionNew("发布") // 互动游戏发起
		,interactionReject("拒绝")  // 互动游戏拒绝，返还
		,interactionOverdua("逾期未处理") //互动游戏逾期，返还
		,buyProduct("购买商品") // 购买商品
		;

	    private String msg;
		
		private AccoutTransactionSourceType(String msg){
			this.msg = msg;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}
	}

	@Column(length = 36, nullable = false)
	private String userId;

	@Column(length = 36, nullable = false)
	private String accountId;

	private String sourceType; //变化来源类型

	private String sourceId;  //变化来源id
    //account 只作为虚拟币,因此不需要区分type
	//private String accountType;// 账户类型 account 中type,已存在 冗余字段看情况增减

	private int balance;// 正数为收入，负数为消费

	private int originValue;// 原始值

	private int currentValue;// 变更值
	
	// 过期时间
	private Date expired;
	
	@Transient
	private String friendlyTime;

	public AccountTransaction(String userId, String accountId, String sourceType, String sourceId, int balance,
			int originValue, int currentValue, Date expired, String name) {
		super();
		this.userId = userId;
		this.accountId = accountId;
		this.sourceType = sourceType;
		this.sourceId = sourceId;
		this.balance = balance;
		this.originValue = originValue;
		this.currentValue = currentValue;
		this.expired = expired;
		this.setName(name);
	}
	
	
}
