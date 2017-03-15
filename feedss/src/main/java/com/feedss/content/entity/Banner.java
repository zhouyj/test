package com.feedss.content.entity;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wangjingqing
 * @date 2016-07-20
 */
@Entity
@Data
@EqualsAndHashCode(callSuper=true)
public class Banner extends BaseEntity{
	public enum BannerStatus{
		NONE, PUBLISHED, DELETE
	}
	
	public enum BannerType{
		Trailer,//预告
		Stream,//直播
		PlayBack,//回放
		UserContent,//个人空间
		H5, //页面
		Blank //无点击效果
		;
	}

	public enum BannerLocation{
		HomePageBanner,	// 首页banner
		HomePageRight,	// 侧边栏
		LiveRoom, //直播间
		Splash, //启动页
		ShopPage, //商城页
		ShopTop,//商城顶部
		ShopLeft,//商城左边
		ShopMiddle//商城中部
	}

	@Column(length=200)
	private String content;//内容
	
	@Column(nullable=false,length=200)
	private String picUrl;//图片路径
	
	@Column(length=200)
	private String title;//标题
	
	@Column(nullable=false)
	private Date playStart;//直播开始时间
	
	@Column(nullable=false)
	private Date playEnd;//直播结束时间
	
	@Column(nullable=false)
	private Date start;//开始时间
	
	@Column(nullable=false)
	private Date end;//结束时间
	
	@Column(nullable=false)
	private String location;//展现位置
	
	@Column(nullable=true)
	private String region;//展现区域，省或地市
	
	@Column(nullable=true)
	private String category;//广告分类

}
