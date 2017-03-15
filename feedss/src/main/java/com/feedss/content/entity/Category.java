package com.feedss.content.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内容分类
 * 
 * @author tangjun
 *
 */
@Entity
@Data
@EqualsAndHashCode(callSuper=true)
public class Category extends BaseEntity {
	
	//public static final String CREATE_CATEGORY_TAG = "CreateCategory";
	
	public enum CategoryTag{
		CreateCategory,//创建直播分类
		VR,//vr直播
		Popular,//最热
		Other;//其他
		
	}
	//分类类型
	public enum CategoryType{
		ALL,//所有的
		OnLine,//在线
		PlayBack,//回放
		Shop,//商城分类
		AD//广告
	}
	public enum Status{
		NORMAL
		,DEL
	}
	private String parentId;	//父分类
	private boolean visiable;// 是否展现
//	@Column
//	private int sort;//排序
	
	private int streamCount;//直播量
	
	private int backCount;//回放数量
	
	private boolean showInHomePageModel;//是否在首页列表中展现
	
	private boolean showInRightModel;//是否在右侧栏展现
	
	@Transient
	private List<Category> childList;
	
}
