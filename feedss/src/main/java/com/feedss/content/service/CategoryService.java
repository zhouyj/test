package com.feedss.content.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.util.ConvertUtil;
import com.feedss.base.util.RedisUtil.KeyType;
import com.feedss.content.entity.Category;
import com.feedss.content.entity.Category.CategoryTag;
import com.feedss.content.entity.Category.CategoryType;
import com.feedss.content.repository.CategoryRepository;

@Component
public class CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private StreamService streamService;

	@Autowired
	private EntityManager entityManager;

	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate;


	public JSONObject getCreateCategory(){
		// 获取创建直播的分类列表
		JSONObject creatCategory = (JSONObject) redisTemplate.opsForValue().get(KeyType.CreateCategory.name());
		if (creatCategory == null || !creatCategory.containsKey("list")) {
			creatCategory = new JSONObject();
			List<Category> categoryList = selectCategoryList(CategoryType.ALL.ordinal());// 查询分类列表
			List<Map<String, Object>> reList = new ArrayList<Map<String, Object>>();// 返回list

			for (Category category : categoryList) {
				if (category.getTags().contains(CategoryTag.CreateCategory.name())
						|| category.getTags().contains(CategoryTag.Other.name())) {
					Map<String, Object> tempMap = new HashMap<String, Object>();
					tempMap.put("categoryId", category.getUuid());// 分类ID
					tempMap.put("name", ConvertUtil.objectToString(category.getName(), false));// 分类名称
					tempMap.put("imageUrl", ConvertUtil.objectToString(category.getDescription(), false));// 分类图标
					reList.add(tempMap);
				}
			}
			if (reList != null && !reList.isEmpty()) {
				creatCategory.put("list", reList);
				creatCategory.put("totalCount", categoryList.size());
				redisTemplate.opsForValue().set(KeyType.CreateCategory.name(), creatCategory, 5, TimeUnit.MINUTES);
			}
		}
		return creatCategory;
	}
	
	public JSONObject getShowCategory() {
		// 获取首页的播放分类
		JSONObject showCategory = (JSONObject) redisTemplate.opsForValue().get(KeyType.ShowCategory.name());
		if (showCategory == null || !showCategory.containsKey("onlineList") || showCategory.containsKey("")) {
			showCategory = new JSONObject();
			List<Category> onlineList = selectCategoryList(CategoryType.OnLine.ordinal());// 在线
			List<Category> playBackList = selectCategoryList(CategoryType.PlayBack.ordinal());// 回放
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
			if (onlineList != null && !onlineList.isEmpty()) {
				showCategory.put("onlineList", online);
			}
			if (playBackList != null && !playBack.isEmpty()) {
				showCategory.put("playbackList", playBack);
			}
			redisTemplate.opsForValue().set(KeyType.ShowCategory.name(), showCategory, 5, TimeUnit.MINUTES);
		}
		return showCategory;
	}

	public Category saveCategory(String categoryId, String userId, String name, String parentId, String showInHome,
			String showInRight, String visiable, String sort, CategoryType type, String imageUrl) {
		// 获取最大的sort排序
		Category category = new Category();
		if (StringUtils.isNotBlank(categoryId)) {
			category = selectCategory(categoryId);
			if (category == null) {
				return null;
			}
		}
		if (StringUtils.isBlank(categoryId)) {
			category.setCreator(userId);
			category.setCreated(new Date());
			category.setVisiable(true);
			category.setBackCount(0);
			category.setType(null == type ? CategoryType.ALL.name() : type.name());
			category.setTags(CategoryTag.CreateCategory.name());
		}
		if (StringUtils.isNotBlank(name)) {
			category.setName(name);
		}
		if (StringUtils.isNotBlank(parentId)) {
			category.setParentId(parentId);
		}

		if (StringUtils.isNotBlank(showInHome)) {
			category.setShowInHomePageModel(StringUtils.equals(showInHome, "true"));
		}

		category.setShowInRightModel(StringUtils.equals(showInRight, "true"));

		if (StringUtils.isNotBlank(visiable)) {
			category.setVisiable(StringUtils.equals(visiable, "true"));
		}
		
		if(StringUtils.isNotBlank(imageUrl)){
			category.setDescription(imageUrl);
		}

		Integer maxSort = seletSortMax(parentId);
		maxSort = maxSort == null ? 0 : maxSort;
		category.setSort(++maxSort);

		if (StringUtils.isNotBlank(sort) && StringUtils.isNumeric(sort)) {
			category.setSort(Integer.valueOf(sort));
		}
		category.setUpdated(new Date());
		category.setUpdater(userId);
		Category one = categoryRepository.save(category);
		return one;
	}

	@Transactional
	public boolean deleteCategory(String categoryId) {
		Category category = selectCategory(categoryId);
		if (category == null) {
			return false;
		}
		int sort = category.getSort();// 当前的排序
		category.setVisiable(false);
		category.setStatus(Category.Status.DEL.ordinal());
		save(category);
		// 跟新排序
		updateSortSubtract(sort);

		// 更新分类下直播
		Category otherCtg = findByTagsAndVisiable(CategoryTag.Other.name());
		if (otherCtg != null) {
			otherCtg.setStreamCount(category.getStreamCount() + otherCtg.getStreamCount());
			otherCtg.setBackCount(category.getBackCount() + otherCtg.getBackCount());
			save(otherCtg);// 更新播放量
			streamService.updateStreamCategory(otherCtg.getUuid(), category.getUuid());// 更新直播分类
		}
		return true;
	}

	/**
	 * 分类列表
	 * 
	 * @param type
	 *            类型 0:全部 1：直播 2：回放 CategoryType，默认包含全部
	 * @return 分类列表
	 */
	public List<Category> selectAllCategory(CategoryType type) {
		final ArrayList<String> typeList = new ArrayList<>();
		typeList.add(CategoryType.ALL.name());
		if (null != type && CategoryType.ALL != type) {
			typeList.add(type.name());
		}
		return categoryRepository.selectCategoryList(typeList, true, new Sort(Direction.ASC, "sort"));
	}

	/**
	 * 分类列表
	 * 
	 * @param type
	 *            类型 0:全部 1：直播 2：回放 CategoryType，默认包含全部
	 * @return 分类列表
	 */
	public List<Category> selectCategoryByType(CategoryType type) {
		final ArrayList<String> typeList = new ArrayList<>();
		typeList.add(type.name());
		return categoryRepository.selectCategoryList(typeList, true, new Sort(Direction.ASC, "sort"));
	}

	/**
	 * 查询分类列表<br>
	 * 
	 * @param type
	 *            0:全部 1：直播 2：回放 CategoryType
	 */
	public List<Category> selectCategoryList(Integer type) {
		String cType = type.intValue() == 1 ? CategoryType.OnLine.toString() : CategoryType.PlayBack.toString();
		String types[] = { CategoryType.ALL.toString(), cType };
		return categoryRepository.selectChildCategoryList(Arrays.asList(types), true, new Sort(Direction.ASC, "sort"));
	}

	/**
	 * 查询分类结构
	 * 
	 * @param types
	 * @param visiable
	 * @param sort
	 * @return
	 */
	public List<Category> selectCategoryListWithChild(CategoryType type, boolean visiable, boolean withAll) {

		String types[];
		if(withAll && CategoryType.ALL != type){
			types =new String[] { CategoryType.ALL.toString(), type.toString() };
		}else{
			types =new String[] { type.toString() };
		}

		Map<String, Object> paraMap = new HashMap<String, Object>();
		StringBuilder sqlSB = new StringBuilder(
				"select c from Category c where parentId is null and c.type in :types and status=0");

		if (visiable) {
			sqlSB.append(" and c.visiable=:visiable");
			paraMap.put("visiable", visiable);
		}
		paraMap.put("types", Arrays.asList(types));

		sqlSB.append(" order by sort");
		TypedQuery<Category> parentList = entityManager.createQuery(sqlSB.toString(), Category.class).setFirstResult(0)
				.setMaxResults(100);
		;
		paraMap.forEach((k, v) -> {
			parentList.setParameter(k, v);
		});
		List<Category> parents = parentList.getResultList();

		// List<Category> parents =
		// categoryRepository.selectParentCategoryList(Arrays.asList(types),true,
		// new Sort(Direction.ASC, "sort"));
		if (parents == null || parents.isEmpty())
			return new ArrayList<>();
		for (Category p : parents) {
			List<Category> childs = selectByParent(p.getUuid(), visiable);
			p.setChildList(childs);
		}

		return parents;
	}

	/**
	 * 根据分类查找子分类
	 * 
	 * @param parentId
	 * @param visiable
	 * @return
	 */
	public List<Category> selectByParent(String parentId, boolean visiable) {

		Map<String, Object> paraMap = new HashMap<String, Object>();
		StringBuilder sqlSB = new StringBuilder("select c from Category c where parentId= :parentId and status=0");
		paraMap.put("parentId", parentId);
		if (visiable) {
			sqlSB.append(" and c.visiable=:visiable");
			paraMap.put("visiable", visiable);
		}

		sqlSB.append(" order by sort");
		TypedQuery<Category> listResult = entityManager.createQuery(sqlSB.toString(), Category.class).setFirstResult(0)
				.setMaxResults(100);
		;
		paraMap.forEach((k, v) -> {
			listResult.setParameter(k, v);
		});
		return listResult.getResultList();
	}

	/**
	 * 获取主页模块列表中展现的
	 * 
	 * @return
	 */
	public List<Category> selectCategoryShowInMainPage() {
		return categoryRepository.selectCategoryShowInMainPage(new Sort(Direction.ASC, "sort"));
	}

	/**
	 * 在首页右侧模块中展现的
	 * 
	 * @return
	 */
	public List<Category> selectCategoryShowInRight() {
		return categoryRepository.selectCategoryShowInRightPage(new Sort(Direction.ASC, "sort"));
	}

	/**
	 * 查询sort最大值<br>
	 * 
	 * @return
	 */
	public Integer seletSortMax(String parentId) {
		return categoryRepository.selectMaxSort(parentId);
	}

	/**
	 * 保存category<br>
	 * 
	 * @return
	 */
	@Transactional
	public Category save(Category category) {
		return categoryRepository.save(category);
	}

	public Category selectCategory(String categoryId) {
		return categoryRepository.findOne(categoryId);
	}
	
	/**
	 * 面包屑导航分类列表
	 * @param categoryId 当前分类
	 * @return 分类列表
	 */
	public List<Category> selectCategoryForBread(String categoryId) {
		ArrayList<Category> categoryList = new ArrayList<>();
		if(StringUtils.isBlank(categoryId)){
			return categoryList;
		}
		Category category = categoryRepository.findOne(categoryId);
		if(null == category){
			return categoryList;
		}
		while(null != category){
			categoryList.add(category);
			if(StringUtils.isBlank(category.getParentId())){
				break;
			}
			category = categoryRepository.findOne(category.getParentId());
		}
		return categoryList;
	}

	/**
	 * 更新排序<br>
	 */
	@Transactional
	public Integer updateSortSubtract(Integer sort) {
		return categoryRepository.updateSortSubtract(sort);
	}

	@Transactional
	public Integer updateStreamCount(int streamCount, int backCount, String categoryId) {
		return categoryRepository.updateStreamCount(streamCount, backCount, categoryId);
	}

	/**
	 * 根据tags查询分了信息<br>
	 * 
	 * @param tags
	 * @return Category
	 */
	public Category findByTagsAndVisiable(String tags) {
		return categoryRepository.findByTagsAndVisiable(tags, true);
	}
}
