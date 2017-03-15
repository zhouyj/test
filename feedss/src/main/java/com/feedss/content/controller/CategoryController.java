package com.feedss.content.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.util.ConvertUtil;
import com.feedss.content.entity.Category;
import com.feedss.content.entity.Category.CategoryTag;
import com.feedss.content.entity.Category.CategoryType;
import com.feedss.content.service.ArticleService;
import com.feedss.content.service.CategoryService;
import com.feedss.content.service.StreamService;

/**
 * 分类API
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

	@Autowired
	private CategoryService categoryService;
	@Autowired
	private StreamService streamService;
	@Autowired
	private ArticleService articleService;

	/**
	 * 开机拉取分类<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<Object> get(HttpServletRequest request, @RequestBody String body) {
		List<Category> onlineList = categoryService.selectCategoryList(CategoryType.OnLine.ordinal());// 在线
		List<Category> playBackList = categoryService.selectCategoryList(CategoryType.PlayBack.ordinal());// 回放
		Map<String, Object> map = new HashMap<String, Object>();

		// 遍历在线分类
		List<Map<String, Object>> online = new ArrayList<Map<String, Object>>();
		for (Category category : onlineList) {
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("categoryId", category.getUuid());// 分类ID
			tempMap.put("name", ConvertUtil.objectToString(category.getName(), false));// 分类名称
			tempMap.put("imageUrl", ConvertUtil.objectToString(category.getDescription(), false));// 分类图标
			online.add(tempMap);
		}

		// 遍历回放分类
		List<Map<String, Object>> playBack = new ArrayList<Map<String, Object>>();
		for (Category category : playBackList) {
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("categoryId", category.getUuid());// 分类ID
			tempMap.put("name", ConvertUtil.objectToString(category.getName(), false));// 分类名称
			tempMap.put("imageUrl", ConvertUtil.objectToString(category.getDescription(), false));// 分类图标
			playBack.add(tempMap);
		}
		map.put("playbackList", playBack);
		map.put("onlineList", online);
		return JsonResponse.success(map);
	}

	/**
	 * 创建流的分类列表<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public ResponseEntity<Object> list(HttpServletRequest request, @RequestBody String body) {
		List<Category> categoryList = categoryService.selectCategoryList(CategoryType.ALL.ordinal());// 查询分类列表
		List<Map<String, Object>> reList = new ArrayList<Map<String, Object>>();// 返回list
		for (Category category : categoryList) {
			if (category.getTags().contains(CategoryTag.CreateCategory.name())
					|| category.getTags().contains(CategoryTag.Other.name())) {
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("categoryId", category.getUuid());// 分类ID
				tempMap.put("name", ConvertUtil.objectToString(category.getName(), false));// 分类名称
				reList.add(tempMap);
			}
		}
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("list", reList);
		reMap.put("totalCount", categoryList.size());
		return JsonResponse.success(reMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getDeepCategory", method = RequestMethod.POST)
	public ResponseEntity<Object> getDeepCategory(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		boolean visible = true;

		String visibleStr = body.getString("visible");
		if (StringUtils.isNotBlank(visibleStr)) {
			visible = !StringUtils.equals(visibleStr, "false");
		}
		Map<String, Object> map = new HashMap<String, Object>();
		List<Category> categories = categoryService.selectCategoryListWithChild(CategoryType.ALL, visible, false);
		map.put("categoryList", categories);
		return JsonResponse.success(map);
	}

	/**
	 * 添加分类<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public ResponseEntity<Object> add(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String categoryId = body.getString("categoryId");
		// 获取最大的sort排序
		Category category = new Category();
		if (StringUtils.isNotBlank(categoryId)) {
			category = categoryService.selectCategory(categoryId);
			if (category == null) {
				return JsonResponse.fail(ErrorCode.INTERNAL_FAILURE);
			}
		}
		if (StringUtils.isBlank(categoryId)) {
			category.setCreator(request.getHeader("userId"));
			category.setCreated(new Date());
			category.setVisiable(true);
			category.setBackCount(0);
			category.setType(CategoryType.ALL.name());
			category.setTags(CategoryTag.CreateCategory.name());
		}
		if (StringUtils.isNotBlank(body.getString("name"))) {
			category.setName(body.getString("name"));
		}
		String parentId = body.getString("parentId");
		if (StringUtils.isNotBlank(parentId)) {
			category.setParentId(parentId);
		}

		String showInHome = body.getString("showInHome");
		if (StringUtils.isNotBlank(showInHome)) {
			category.setShowInHomePageModel(StringUtils.equals(showInHome, "true"));
		}

		String showInRight = body.getString("showInRight");
		if (StringUtils.isNotBlank(showInRight)) {
			category.setShowInRightModel(StringUtils.equals(showInRight, "true"));
		}

		String visiable = body.getString("visible");
		if (StringUtils.isNotBlank(visiable)) {
			category.setVisiable(StringUtils.equals(visiable, "true"));
		}

		Integer maxSort = categoryService.seletSortMax(parentId);
		maxSort = maxSort == null ? 0 : maxSort;
		category.setSort(++maxSort);

		if (StringUtils.isNotBlank(body.getString("sort")) && StringUtils.isNumeric(body.getString("sort"))) {
			category.setSort(Integer.valueOf(body.getString("sort")));
		}
		category.setUpdated(new Date());
		category.setUpdater(request.getHeader("userId"));
		Category c = categoryService.save(category);
		return JsonResponse.success(c);
	}

	/**
	 * 删除分类<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public ResponseEntity<Object> delete(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String categoryId = body.getString("categoryId");
		if (StringUtils.isBlank(categoryId)) {
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}
		Category category = categoryService.selectCategory(categoryId);
		if (category == null) {
			return JsonResponse.fail(ErrorCode.ERROR);
		}
		int sort = category.getSort();// 当前的排序
		category.setVisiable(false);
		category.setStatus(Category.Status.DEL.ordinal());
		categoryService.save(category);
		// 跟新排序
		categoryService.updateSortSubtract(sort);

		// 更新分类下直播
		Category otherCtg = categoryService.findByTagsAndVisiable(CategoryTag.Other.name());
		if (otherCtg != null) {
			otherCtg.setStreamCount(category.getStreamCount() + otherCtg.getStreamCount());
			otherCtg.setBackCount(category.getBackCount() + otherCtg.getBackCount());
			categoryService.save(otherCtg);// 更新播放量
			streamService.updateStreamCategory(otherCtg.getUuid(), category.getUuid());// 更新直播分类
		}
		//内容相关处理
		articleService.delCategory(categoryId);
		return JsonResponse.success();
	}

	/**
	 * 分类列表<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/ctgList", method = RequestMethod.POST)
	public ResponseEntity<Object> ctgList(HttpServletRequest request, @RequestBody String body) {
		List<Category> categoryList = categoryService.selectCategoryList(0);// 查询分类列表
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("count", categoryList == null ? 0 : categoryList.size());
		reMap.put("list", categoryList);
		return JsonResponse.success(reMap);
	}

	/**
	 * 分类列表<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/categoryAll", method = RequestMethod.POST)
	public ResponseEntity<Object> categoryAll(HttpServletRequest request, @RequestBody String body) {
		List<Category> categoryList = categoryService.selectAllCategory(CategoryType.ALL);
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("count", categoryList == null ? 0 : categoryList.size());
		reMap.put("list", categoryList);
		return JsonResponse.success(reMap);
	}
}
