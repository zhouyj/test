package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品表
 * 
 * 应该能放在这里！
 * @author zhangjie
 *
 */
@Entity
@Data
@EqualsAndHashCode(callSuper=true)
public class UsrProduct extends BaseEntity{

	//private String name;//父类

	public enum ProductType {
		 GIFT,VIRTUAL, NORMAL
	}
	
	@Column(length = 800, nullable = false)
	private String pic;//商品图片
	
	private int price;//价格
	
	//private status  父类
	
	// 分类ID
	@Column(length = 36, nullable = true)
	private String category;

	//库存
	@Column
	private Integer stocks;

}
