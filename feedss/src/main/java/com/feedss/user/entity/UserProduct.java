package com.feedss.user.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

/**
 * Created by qin.qiang on 2016/7/21 0021.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserProduct extends BaseEntity {

	/** 待发货 */
	public static final int STATUS_DELIVERY_READY = 0;
	/** 已发货 */
	public static final int STATUS_DELIVERYED = 1;

	private String userId; // 用户id

	private String productId; // 商品id

	private int productNum; // 商品数量

}
