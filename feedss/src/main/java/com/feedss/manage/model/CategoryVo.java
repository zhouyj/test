package com.feedss.manage.model;

import lombok.Data;

import java.util.List;

/**
 * 内容分类
 * 
 * @author tangjun
 *
 */
@Data
public class CategoryVo {
	
	private String uuid;
	private String name;
	private String parentId;

	private boolean visiable;// 是否展现
	private int sort;//排序
	private int streamCount;//直播量

	private int backCount;//回放数量
	private boolean showInHomePageModel;//是否在首页列表中展现
	private boolean showInRightModel;//是否在右侧栏展现

	private List<CategoryVo> childList;	// 子分类
}
