package com.feedss.manage.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.content.entity.Article;
import com.feedss.content.entity.Article.ArticleStatus;
import com.feedss.content.entity.Category;
import com.feedss.content.service.ArticleService;
import com.feedss.content.service.CategoryService;
import com.feedss.manage.entity.PageVo;
import com.feedss.manage.util.Constants;
import com.feedss.user.model.UserVo;

/**
 * @date 2016-09-22
 */
@Controller
@RequestMapping("/manage/articles")
public class ManageArticlesController {

	@Autowired
	private ArticleService articleService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	EntityManager entityManager;

	/**
	 * 查询stream列表<br>
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/", method = RequestMethod.GET)
	public String list(HttpServletRequest request,HttpSession session, Model model){
		Integer status = request.getParameter("status") == null ? 1 : Integer.valueOf(request.getParameter("status"));
		Integer pageNo = request.getParameter("pageNo") == null ? 0 : Integer.valueOf(request.getParameter("pageNo"));
		Integer pageSize = request.getParameter("pageSize") == null ? 500 : Integer.valueOf(request.getParameter("pageSize"));
		String title = request.getParameter("title");
		String articleId = request.getParameter("articleId");
		UserVo userVo = (UserVo)session.getAttribute(Constants.USER_SESSION);
		List<Category> categories = categoryService.selectCategoryList(0);
//		Long totalCount = articleService.selectArticleCount(null, status);// 查询总数量
		Page<Article> articlePages = articleService.selectArticlePages(null, status, pageNo, pageSize);
//		Pages<Article> pages = new Pages<Article>(totalCount.intValue(), articlePages.getContent());
		
		model.addAttribute("categoryList", categories);
		model.addAttribute("articleId", articleId);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("articles", articlePages.getContent());
		model.addAttribute("totalCount", articlePages.getTotalElements());
		model.addAttribute("pageCount", articlePages.getTotalPages());
		model.addAttribute("status",status);
		model.addAttribute("pageNo", pageNo);
		model.addAttribute("title", title);
		model.addAttribute("userId", userVo.getUuid());
		return "/manage/articles/list";
	}

	/**
	 * 查询stream列表<br>
	 * @param request
	 * @param model
	 * @return
	 */
	@Transactional
	@RequestMapping(value="/listByPage", method = RequestMethod.POST)
	public ResponseEntity<Object> listByPage(HttpServletRequest request,HttpSession session, Model model){
		String limit = request.getParameter("limit");
		String offset = request.getParameter("offset");
		String search = request.getParameter("search");
		String filter = request.getParameter("filter");

		JSONObject articleVo = JSONObject.parseObject(filter, JSONObject.class);
		if (null == articleVo){
			articleVo = new JSONObject();
		}
		String name = articleVo.getString("name");
		String tags = articleVo.getString("tags");
		String publisher = articleVo.getString("publisher");
		Integer status = articleVo.getInteger("status");
		String category = articleVo.getString("category");

		Map<String,Object> paraMap = new HashMap<String,Object>();
		StringBuilder sqlCountSB = new StringBuilder("select count(a) from Article a where 1 = 1");
		StringBuilder sqlSB = new StringBuilder("select a from Article a where 1 = 1");

		if (null != status){
			sqlSB.append(" and a.status = :status ");
			sqlCountSB.append(" and a.status = :status ");
			paraMap.put("status", status);
		}else {
			sqlSB.append(" and a.status != 0 ");
			sqlCountSB.append(" and a.status != 0 ");
		}

		if (StringUtils.isNotBlank(name)){
			sqlSB.append(" and a.name LIKE :name");
			sqlCountSB.append(" and a.name LIKE :name");
			paraMap.put("name", "%" + name + "%");
		}

		if (StringUtils.isNotBlank(tags)){
			sqlSB.append(" and a.tags LIKE :tags");
			sqlCountSB.append(" and a.tags LIKE :tags");
			paraMap.put("tags", "%" + tags + "%");
		}

		if (StringUtils.isNotBlank(category)){
			sqlSB.append(" and a.category = :category");
			sqlCountSB.append(" and a.category = :category");
			paraMap.put("category", category);
		}

//		Logger.getRootLogger().error(paraMap.toString());

		//查询数目
		Query query = entityManager.createQuery(sqlCountSB.toString());
		paraMap.forEach(query::setParameter);

		long totalCount = (long) query.getSingleResult();

		int pageSize = null == limit ? 20 : Integer.valueOf(limit);
		int pageNo = null == offset ? 0 : (Integer.valueOf(offset) - 1);

		sqlSB.append(" order by a.status, a.updated desc");
		TypedQuery<Article> result = entityManager.createQuery(sqlSB.toString(), Article.class)
				.setFirstResult(pageSize * pageNo).setMaxResults(pageSize);
		paraMap.forEach(result::setParameter);

		List<Article> articleList = result.getResultList();

		Logger.getRootLogger().debug("result --- " + articleList);

		PageVo<Article> pageVo = new PageVo<>();
		pageVo.setRows(articleList);
		pageVo.setTotal((int) totalCount);

		return JsonResponse.success(pageVo);
	}

	/**
	 * 添加页面<br>
	 *
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String add(HttpServletRequest request, HttpSession session, Model model) {
		String articleId = request.getParameter("articleId");

		UserVo user = (UserVo)session.getAttribute(Constants.USER_SESSION);
		Article articleVo = null;
		if (!StringUtils.isEmpty(articleId)){
			articleVo = articleService.viewArticle(articleId, null, null);
		}

		if (null == articleVo){
			articleVo = new Article();
			articleVo.setCategory("");
			articleVo.setCover("");
			articleVo.setCreator("");
			articleVo.setTags("");
			articleVo.setName("");
			articleVo.setSource("");
			articleVo.setStatus(4);
			articleVo.setDescription("");
			articleVo.setStreamId("");
			articleVo.setPics("");
			articleVo.setText("");
		}
		model.addAttribute("userId", user.getUuid());
		List<Category> categories = categoryService.selectCategoryList(0);
		model.addAttribute("categories", categories);
		model.addAttribute("article", articleVo);

		return "/manage/articles/add";
	}

	/**
	 * 添加文章
	 *
	 * * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public ResponseEntity<Object> save(HttpServletRequest request,HttpSession session) {
		String articleId = request.getParameter("articleId");
		String articleTitle = request.getParameter("title");
		String articleSource = request.getParameter("source");
		String articleContent = request.getParameter("content");
		String articleTags = request.getParameter("tags");
		String articleCategory = request.getParameter("category");
		String articleCover = request.getParameter("cover");
		String articleStreamId = request.getParameter("streamId");
		String saveType = request.getParameter("saveType");
		if(StringUtils.isBlank(saveType)){
			saveType = "2";//默认已发布
		}
		
		Article article = new Article();
		article.setUuid(articleId);
		article.setName(articleTitle);
		article.setSource(articleSource);
		article.setText(articleContent);
		article.setTags(articleTags);
		article.setCategory(articleCategory);
		article.setCover(articleCover);
		article.setStreamId(articleStreamId);
		
		try {
			int status = Integer.parseInt(saveType);
			article.setStatus(status);
		} catch (Exception e) {
			article.setStatus(ArticleStatus.valueOf(saveType).ordinal());
		}
		
		article.setUpdated(new Date());
		article.setUpdater(request.getHeader("userId"));
		
		Article result = articleService.saveArticle(article);
		return result==null ? JsonResponse.fail(ErrorCode.CHECK_PASSWORD_ERROR) : JsonResponse.success(new JSONObject());
	}

	/**
	 * 添加文章
	 *
	 * * @return
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public ResponseEntity<Object> delete(HttpServletRequest request,HttpSession session) {
		String ids = request.getParameter("ids");
		if (StringUtils.isEmpty(ids)){
			return JsonResponse.fail(ErrorCode.CHECK_PASSWORD_ERROR);
		}
		boolean result  =articleService.deleteArticles(ids.split(","));
		return result ? JsonResponse.success(new JSONObject()) : JsonResponse.fail(ErrorCode.CHECK_PASSWORD_ERROR);
	}
}
