package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付记录
 * 
 * @author Administrator
 *
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayRecord extends BaseEntity {

	public static final int UN_PAY = 1;

	public static final int PAY_SUCCES = 2;

	public static final int PAY_FAIL = -1;

	public enum PayMethod{
		WeChat
	}

	@Column(length = 36, nullable = false)
	private String userId;

	@Column(length = 36, nullable = false)
	private String orderNo;// 订单号

	@Column(length = 36, nullable = false)
	private String payMethodId;// 支付方式

	@Column(length = 36)
	private String outOrderNo;// 外部订单号

	@Column(length = 36, nullable = false)
	private int currencyAmount;// 购买虚拟币数

	@Column(length = 36, nullable = false)
	private int moneyAmount; //支付金额(以分为单位)
	
	@Column(length = 36, nullable = true)
	private String percent; //虚拟币和人民币的比例

	private int payStatus;

	@Column(length = 800)
	private String returnMsg;// 支付返回消息

}
