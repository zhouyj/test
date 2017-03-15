package com.feedss.manage.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.ListUtils;

import com.feedss.content.entity.Category;
import com.feedss.content.entity.Category.CategoryType;
import com.feedss.content.service.CategoryService;
import com.feedss.manage.model.NodeData;
import com.feedss.user.model.UserVo;

/**
 * 分类服务<br>
 * @author wangjingqing
 */
@Service
public class ManageCategoryService {
	
	static Log logger = LogFactory.getLog(ManageCategoryService.class);

	@Autowired
	CategoryService categoryService;

	/**
	 * 树形分类列表
	 * 	CategoryVo 结果转换为 NodeData
	 * 		目前支持二级, 支持多级扩展可进行递归处理
	 *
	 * @param user UserVo
	 * @return List<NodeData>
	 */
	public List<NodeData> getCategoryList(CategoryType type){
		List<Category> categorys = categoryService.selectCategoryListWithChild(type, false, false);
		// 以下是转换model, 可转为递归以支持更多级别
		List<NodeData> list = new ArrayList<NodeData>();
		for (Category category : categorys){
			NodeData nodeData = new NodeData();
			if (!ListUtils.isEmpty(category.getChildList())){
				List<NodeData> childList = new ArrayList<NodeData>();
				for (Category child : category.getChildList()){
					NodeData childNode = new NodeData();
					childNode.setKey(child.getUuid());
					childNode.setTitle(child.getName());
					childNode.setTooltip(child.getName());
					childNode.setSort(child.getSort());
					childNode.setFolder(false);
					childNode.setShowInRightModel(child.isShowInRightModel());
					childNode.setShowInHomePageModel(child.isShowInHomePageModel());
					childNode.setVisible(child.isVisiable());
					childNode.setIcon(child.getDescription());
					childList.add(childNode);
				}
				nodeData.setChildren(childList);
				nodeData.setParentId(category.getParentId());
			}else {
				nodeData.setParentId("");
			}

			nodeData.setKey(category.getUuid());
			nodeData.setTitle(category.getName());
			nodeData.setTooltip(category.getName());
			nodeData.setSort(category.getSort());
			nodeData.setShowInHomePageModel(category.isShowInHomePageModel());
			nodeData.setShowInRightModel(category.isShowInRightModel());
			nodeData.setVisible(category.isVisiable());
			nodeData.setFolder(true);
			list.add(nodeData);
		}
		return list;
	}

	public NodeData saveOrUpdate(String parentId, String categoryId,String name,String sort,
								 String showInHome, String showInRight, String visiable, UserVo user, CategoryType type, String imageUrl){
		
//		Category category = new Category();
//		category.setParentId(parentId);
//		category.setUuid(categoryId);
//		category.setName(name);
//		category.setSort(Integer.parseInt(sort));
//		category.setShowInHomePageModel(Boolean.parseBoolean(showInHome));
//		category.setShowInRightModel(Boolean.parseBoolean(showInRight));
//		category.setVisiable(Boolean.parseBoolean(visible));
//		Category result = categoryService.save(category);
		Category result = categoryService.saveCategory(categoryId, user.getUuid(), name, parentId, showInHome, showInRight, visiable, sort, type, imageUrl);

		NodeData nodeData = new NodeData();
		nodeData.setKey(result.getUuid());
		nodeData.setTitle(result.getName());
		nodeData.setTooltip(result.getName());
		nodeData.setData(result.getSort());
		nodeData.setParentId(result.getParentId());
		return nodeData;
	}
//
//	/**
//	 * 删除banner<br>
//	 * @return
//	 */
//	public boolean delete(UserVo user, String categoryId){
//		Map<String,Object> para = new HashMap<String,Object>();
//		para.put("categoryId", categoryId);
//		DataEntity<JSONObject> date = TemplateUtil.postRequest(contentCenter+"/category/delete", user, para, JSONObject.class);
//		if(date == null || date.getCode() > 0 ){
//			return false;
//		}
//		return true;
//	}
}
