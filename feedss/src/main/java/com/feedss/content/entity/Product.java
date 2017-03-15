package com.feedss.content.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品<br>
 * @author wangjingqing
 * @date 2016-07-20
 */
@Entity
@Data
@EqualsAndHashCode(callSuper=true)
public class Product extends BaseEntity{

	@Column(nullable=false)
	private String productId;//商品ID

	@Column(nullable=false)
	private BigDecimal price;//价格
	
	@Column
	private String picUrl;//图片路径
	
	@Column(length=200)
	private String content;//内容
	
	@Column
	private Integer stocks;//库存
	
}
