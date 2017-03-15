package com.feedss.user.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

/**
 * Created by qinqiang on 2016/7/21.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRecord extends BaseEntity{

	public enum RecordType {
		PURCHASE,  //购买
		GIVE,   //赠送
		SALE//折现
	}

	public static final int GIVE_SUCCESS = 0;  //赠送成功,待后台更新接收礼物
	public static final int RECEIVE_SUCCESS = 1; //后台更新接收礼物成功

	@Column(length = 800, nullable = false)
	private String fromUserId;//商品来源

	private String toUserId;// 商品去向

	//private String  transactionType; //交易类型

	private String productId; //商品id

	private int productNum;  //商品数量

	private int transactionAmount; //交易总金额

}
