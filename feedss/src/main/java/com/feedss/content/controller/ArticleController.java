package com.feedss.content.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.content.entity.Article;
import com.feedss.content.entity.Article.ArticleStatus;
import com.feedss.content.service.ArticleService;
import com.feedss.content.service.CategoryService;

/**
 * @author : by AegisLee on 16/11/24.
 */
@RestController
@RequestMapping("/article")
public class ArticleController {
	@Autowired
	ArticleService articleService;
	@Autowired
	CategoryService categoryService;

	Logger logger = Logger.getLogger(getClass());

	/**
	 * 根据分类及状态查询文章列表
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public ResponseEntity<Object> selectArticleList(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String categoryId = body.getString("category");// 分类ID
		Integer status = body.getInteger("status");// 类型 0: 草稿; 1: 已发布; 2: 回收站;
													// 3: 已删除
		Integer pageSize = body.getInteger("pageSize");// 每页显示数量
		pageSize = pageSize == null ? 20 : pageSize;
		Integer pageNo = body.getInteger("pageNo");
		pageNo = pageNo == null ? 0 : --pageNo;

		List<Article> articleList = null;
		long totalCount = articleService.selectArticleCount(categoryId, status);// 查询总数量
		if (totalCount > 0) {
			Page<Article> articlePage = articleService.selectArticlePages(categoryId, status, pageNo, pageSize);
			articleList = articlePage.getContent();
		} else {
			articleList = new ArrayList<Article>();
		}

		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("list", articleList);
		reMap.put("totalCount", totalCount);
		return JsonResponse.success(reMap);
	}

	@ResponseBody
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public ResponseEntity<Object> saveArticle(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String articleId = body.getString("articleId");
		Integer status = body.getInteger("status"); // 保存草稿还是发布;
		String articleTitle = body.getString("title");
		String articleSource = body.getString("source");
		String articleContent = body.getString("content");
		String articleTags = body.getString("tags");
		String articleCategory = body.getString("category");
		String articleCover = body.getString("cover");
		String articleStreamId = body.getString("streamId");

		Article article = null;
		// 如果有文章id, 表示更新文章
		if (StringUtils.isNotBlank(articleId)) {
			article = articleService.selectArticleDetail(articleId);
			if (article == null) {
				return JsonResponse.fail(ErrorCode.INTERNAL_FAILURE);
			} else {
				article.setStatus(status);
				article.setUpdated(new Date());
				article.setUpdater(request.getHeader("userId"));
				if (!StringUtils.isEmpty(articleTitle)) {
					article.setName(articleTitle);
				}
				if (!StringUtils.isEmpty(articleSource)) {
					article.setSource(articleSource);
				}
				if (!StringUtils.isEmpty(articleContent)) {
					article.setText(articleContent);
				}
				if (!StringUtils.isEmpty(articleTags)) {
					article.setTags(articleTags);
				}
				if (!StringUtils.isEmpty(articleCategory)) {
					article.setCategory(articleCategory);
				}
				if (!StringUtils.isEmpty(articleCover)) {
					article.setCover(articleCover);
				}
				if (!StringUtils.isEmpty(articleStreamId)) {
					article.setStreamId(articleStreamId);
				}
				articleService.saveArticle(article);
				return JsonResponse.success(article);
			}
		} else {
			article = new Article();
			article.setCreated(new Date());
			article.setStatus(status);
			article.setName(articleTitle);
			article.setSource(articleSource);
			article.setText(articleContent);
			article.setTags(articleTags);
			article.setCategory(articleCategory);
			article.setCover(articleCover);
			article.setStreamId(articleStreamId);
		}

		article.setUpdated(new Date());
		article.setUpdater(request.getHeader("userId"));
		articleService.saveArticle(article);
		return JsonResponse.success(article);
	}

	@ResponseBody
	@RequestMapping(value = "/detail", method = RequestMethod.POST)
	public ResponseEntity<Object> selectDetail(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = request.getHeader("userId");
		String articleId = body.getString("articleId");
		String appChannel = request.getHeader("appChannel");

		Article article = articleService.viewArticle(articleId, appChannel, userId);
		return JsonResponse.success(article);
	}

	@ResponseBody
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public ResponseEntity<Object> deleteArticle(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String articleIds = body.getString("articleIds");
		if (StringUtils.isEmpty(articleIds)) {
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}
		String[] idArr;
		if (articleIds.contains(",")) {
			idArr = articleIds.split(",");
		} else {
			idArr = new String[1];
			idArr[0] = articleIds;
		}

		articleService.deleteArticles(idArr);
		return JsonResponse.success();
	}

	@ResponseBody
	@RequestMapping(value = "/listGroupByCategory", method = RequestMethod.POST)
	public ResponseEntity<Object> listGroupByCategory(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		Integer articleLimit = body.getInteger("articleLimit");
		if (null == articleLimit) {
			articleLimit = 3;// 默认每个分类只返回3条
		}
		Integer status = body.getInteger("status");// 类型 0: 已删除; 1: 草稿；2: 已发布;
													// 3: 回收站;
		if (null == status) {
			status = ArticleStatus.PUBLISHED.ordinal();// 默认已发布
		}
		return JsonResponse.success(articleService.listGroupByCategory(status, articleLimit));
	}

}
