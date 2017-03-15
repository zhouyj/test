package com.feedss.content.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 * @author wangjingqing
 * @date 2016-08-16
 */
@Entity
@Data
@EqualsAndHashCode(callSuper=true)
public class ReleaseProduct extends BaseEntity{

	public enum ReleaseType {
		Product, // 商品
		Advert// 广告
	}
	@Column(nullable=false)
	private String productId;//uuid
	
	@Column(nullable=false)
	private String streamId;//直播uuid
	
	@Column(nullable=false)
	private BigDecimal price;//价格
	
	@Column
	private String picUrl;//图片路径
	
	@Column
	private String content;//内容
}
