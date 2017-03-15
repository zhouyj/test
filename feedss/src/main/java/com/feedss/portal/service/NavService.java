package com.feedss.portal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.feedss.content.entity.Category;
import com.feedss.content.entity.Category.CategoryType;
import com.feedss.content.service.CategoryService;

/**
 * 导航业务类
 * @author Looly
 *
 */
@Service
public class NavService {
	
	@Autowired
	private CategoryService categoryService;
	
	/**
	 * 生成导航树，最多二级导航
	 * 
	 * @return 导航树
	 */
	public JSONArray shopNavTree() {
		return navTree(CategoryType.Shop);
	}
	
	/**
	 * 生成导航树，最多二级导航
	 * 
	 * @return 导航树
	 */
	public JSONArray navTree() {
		return navTree(CategoryType.ALL);
	}
	
	/**
	 * 生成导航树，最多二级导航
	 * 
	 * @return 导航树
	 */
	public JSONArray navTree(CategoryType type) {
		List<Category> categoryList = categoryService.selectCategoryByType(type);
		if (null == categoryList) {
			return new JSONArray(0);
		}

		final JSONArray nav = new JSONArray();
		for (Category category : categoryList) {
			String parentId = category.getParentId();
			if (StringUtils.isEmpty(parentId)) {
				// 顶层分类
				nav.add(category);
			}
			
			// 对于每个分类，都要重新遍历一次列表找到其子分类
			List<Category> childList = new ArrayList<>();
			for (Category childCategory : categoryList) {
				if (category.getUuid().equals(childCategory.getParentId())) {
					childList.add(childCategory);
				}
			}
			category.setChildList(childList);
		}
		return nav;
	}
	
	/**
	 * 面包屑导航
	 * @param categoryId
	 * @return
	 */
	public List<Category> breadNav(String categoryId){
		List<Category> categoryForBread = categoryService.selectCategoryForBread(categoryId);
		Collections.reverse(categoryForBread);
		return categoryForBread;
	}
}
