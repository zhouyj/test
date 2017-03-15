package com.feedss.app;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * App客户端按钮
 * 
 * @author tangjun
 *
 */
@Data
@Entity
public class Button extends BaseEntity {

	/**
	 * 父按钮，即菜单
	 */
	private String parentId;

	/**
	 * 位置：leftTop,rightTop,tabs
	 */
	private String position;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 类型：Native,Html5
	 */
	private String type;

	/**
	 * 图标URL，如：http://live.feedss.com/images/icons/home.png
	 */
	private String iconUrl;
	
	/**
	 * 选中的图片URL，如：http://live.feedss.com/images/icons/homeSelected.png
	 */
	private String iconSelectedUrl;

	/**
	 * Native，如：native://message，native://rank
	 * Html5，如：http://live.feedss.com/h5/xxx.html
	 */
	private String viewUrl;

	/**
	 * 参数
	 */
	private String parameter;

	/**
	 * 子按钮（菜单）
	 */
	@Transient
	private List<Button> buttons;
}
