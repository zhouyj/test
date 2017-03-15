package com.feedss.manage.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.content.entity.Category;
import com.feedss.content.entity.Category.CategoryType;
import com.feedss.content.service.ArticleService;
import com.feedss.content.service.CategoryService;
import com.feedss.manage.model.NodeData;
import com.feedss.manage.service.ManageCategoryService;
import com.feedss.manage.util.Constants;
import com.feedss.user.model.UserVo;
/**
 * 分类<br>
 * @author wangjingqing
 * @date 2016-08-20
 */
@Controller
@RequestMapping("/manage/category")
public class ManageCategoryController {

	@Autowired
	private ManageCategoryService manageCategoryService;
	
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private ArticleService articleService;

	private boolean isTree = true;	// false: 非树形保持原接口,  true: 树形, 为新接口

	@RequestMapping(value="/", method=RequestMethod.GET)
	public String list(HttpServletRequest request, HttpSession session, Model model){
//		UserVo userVo = (UserVo)session.getAttribute(Constants.USER_SESSION);
		String type = request.getParameter("type");
		if(StringUtils.isBlank(type)){
			type = "ALL";
		}
		model.addAttribute("type", type);
		if (isTree){
			model.addAttribute("categorys", manageCategoryService.getCategoryList(CategoryType.valueOf(type)));
			return "/manage/category/tree";
		}else {
			model.addAttribute("categorys", categoryService.selectCategoryByType(CategoryType.valueOf(type)));
			return "/manage/category/list";
		}
	}
	
	/**
	 * 添加或修改分类<br>
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/add",method=RequestMethod.POST)
	public Object post(HttpServletRequest request, HttpSession session, Model model){
		UserVo userVo = (UserVo)session.getAttribute(Constants.USER_SESSION);
		String name = request.getParameter("name");
		String imageUrl = request.getParameter("imageUrl");
		if(StringUtils.isBlank(name)){
			return JsonResponse.fail(ErrorCode.TITLE_IS_EMPTY);
		}
		String categoryId = request.getParameter("categoryId");
		String parentId = request.getParameter("parentId");
		CategoryType type = null;
		String typeStr = request.getParameter("type");
		if(StringUtils.isNotBlank(typeStr)){
			type = CategoryType.valueOf(typeStr);
		}
		
		
		Category result = categoryService.saveCategory(categoryId, userVo.getUuid(), name, parentId, null, null, null, null, type, imageUrl);
		if(result==null){
			return JsonResponse.fail(ErrorCode.INTERNAL_ERROR);
		}
		return JsonResponse.success(new ArrayList<>());
	}

	/**
	 * 添加或修改分类<br>
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/add_update",method=RequestMethod.POST)
	public ResponseEntity<Object> addOrUpdate(HttpServletRequest request, HttpSession session, Model model){
		UserVo userVo = (UserVo)session.getAttribute(Constants.USER_SESSION);
		String name = request.getParameter("name");
		String categoryId = request.getParameter("categoryId");
		String parentId = request.getParameter("parentId");
		String sort = request.getParameter("sort");
		String showInHome = request.getParameter("showInHome");
		String showInRight = request.getParameter("showInRight");
		String visible = request.getParameter("visible");
		CategoryType type = null;
		String typeStr = request.getParameter("type");
		if(StringUtils.isNotBlank(typeStr)){
			type = CategoryType.valueOf(typeStr);
		}
		String imageUrl = request.getParameter("imageUrl");

		NodeData nodeData = manageCategoryService.saveOrUpdate(parentId, categoryId, name, sort, showInHome,
				showInRight, visible, userVo, type, imageUrl);

		if(nodeData == null){
			return JsonResponse.fail(ErrorCode.INTERNAL_ERROR);
		}
		return JsonResponse.success(nodeData);
	}

	/**
	 * 删除<br>
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/delete",method=RequestMethod.POST)
	public ResponseEntity<Object> delete(HttpServletRequest request, HttpSession session, Model model){
//		UserVo userVo = (UserVo)session.getAttribute(Constants.USER_SESSION);
		String categoryId = request.getParameter("categoryId");
		if(StringUtils.isBlank(categoryId)){
			return JsonResponse.fail(ErrorCode.TITLE_IS_EMPTY);
		}
		boolean status = categoryService.deleteCategory(categoryId);
		if(!status){
			return JsonResponse.fail(ErrorCode.INTERNAL_ERROR);
		}

		//内容相关处理
		articleService.delCategory(categoryId);
		return JsonResponse.success("");
	}
	
	/**
	 * 修改分类<br>
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/sort",method=RequestMethod.POST)
	public ResponseEntity<Object> sort(HttpServletRequest request,HttpSession session, Model model){
		UserVo userVo = (UserVo)session.getAttribute(Constants.USER_SESSION);
		String sort = request.getParameter("sort");
		String parentId = request.getParameter("parentId");
		String categoryId = request.getParameter("categoryId");
		if(!StringUtils.isNumeric(sort)){//sort数字判断
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}
		if(StringUtils.isBlank(categoryId)){//分了ID
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}
		Category result = categoryService.saveCategory(categoryId, userVo.getUuid(), null, parentId, null, null, null, sort, null, null);
		if(result==null){
			return JsonResponse.fail(ErrorCode.INTERNAL_ERROR);
		}
		return JsonResponse.success(new ArrayList<>());
	}
}
