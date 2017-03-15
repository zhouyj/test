package com.feedss.content.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.Constants;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.content.entity.Article;
import com.feedss.content.entity.Article.ArticleStatus;
import com.feedss.content.entity.Category;
import com.feedss.content.repository.ArticleRepository;
import com.feedss.user.entity.AccountTransaction.AccoutTransactionSourceType;
import com.feedss.user.entity.Favorite;
import com.feedss.user.service.AccountService;
import com.feedss.user.service.UserFavoriteService;
import com.feedss.user.service.UserService;

/**
 * @author : by AegisLee on 16/11/24.
 */
@Transactional
@Component
public class ArticleService {
	@Autowired
	ArticleRepository articleRepository;
	@Autowired
	CategoryService categoryService;
	@Autowired
	UserService userService;
	@Autowired
	UserFavoriteService favoriteService;
	@Autowired
	AccountService accountService;
	@Autowired
	ConfigureUtil configureUtil;

	public Map<String, Object> listGroupByCategory(int status, int articleLimit) {
		List<Category> mainCategoryList = categoryService.selectCategoryShowInMainPage();
		List<Category> rightCategoryList = categoryService.selectCategoryShowInRight();

		JSONArray mainCategoryJSONArray = getCategoryJSON(mainCategoryList, status, articleLimit);
		JSONArray rightCategoryJSONArray = getCategoryJSON(rightCategoryList, status, articleLimit);

		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("mainList", mainCategoryJSONArray);
		reMap.put("rightList", rightCategoryJSONArray);
		return reMap;
	}

	private JSONArray getCategoryJSON(List<Category> categoryList, int status, int articleLimit) {
		JSONArray categoryJSONArray = new JSONArray();
		if (categoryList != null && !categoryList.isEmpty()) {
			for (Category category : categoryList) {
				Page<Article> articlePage = selectArticlePages(category.getUuid(), status, 0/* 第一页 */, articleLimit);
				JSONObject categoryJSON = (JSONObject) JSONObject.toJSON(category);
				categoryJSON.put("articles", articlePage);
				categoryJSONArray.add(categoryJSON);
			}
		}

		return categoryJSONArray;
	}

	public Article viewArticle(String articleId, String appChannel, String userId) {
		Article article = selectArticleDetail(articleId);
		if (StringUtils.isNotEmpty(appChannel) && appChannel.equalsIgnoreCase("web") && article != null) {
			addviewCount(articleId, 1);
			// TODO 抽象 aop中完成
			if (StringUtils.isNotEmpty(userId)) {
				Map<String, Object> extMap = new HashMap<String, Object>();
				extMap.put("name", article.getName());
				extMap.put("cover", article.getCover());
				extMap.put("source", article.getSource());
				extMap.put("updated", article.getUpdated());
				extMap.put("viewCount", article.getViewCount());
				// 分类信息 待确认
				// userService.addLog(userId, "article", "VIEW", articleId, extMap);
				Favorite one = favoriteService.add(userId, "article", articleId, "VIEW", JSONObject.toJSONString(extMap));
				// 奖励
				int viewArticleRewardCount = configureUtil.getConfigIntValue(Constants.REWARD_ARTICLEDETAIL_COUNT);
				if (one != null && viewArticleRewardCount > 0) {
					accountService.reward(userId, viewArticleRewardCount, articleId, AccoutTransactionSourceType.systemPresentViewArticle);
				}
			}
		}
		return article;
	}

	@Transactional
	public Long selectArticleCount(String categoryId, Integer status) {
		if (!StringUtils.isEmpty(categoryId) && null != status && status > 0) {
			return articleRepository.selectArticleCount(categoryId, status);
		} else if (null != status && status > 0) {
			return articleRepository.selectArticleCountByStatus(status);
		} else if (!StringUtils.isEmpty(categoryId)) {
			return articleRepository.selectArticleCountByCategory(categoryId);
		}
		return articleRepository.selectArticleCount();
	}

	public List<Article> selectArticleListAll() {
		return articleRepository.selectArticleList();
	}

	public Page<Article> selectArticlePages(String categoryId, Integer status, Integer pageNo, Integer pageSize) {
		Sort sort = new Sort(Direction.DESC, "updated");// 排序
		PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);
		
		if (false == StringUtils.isEmpty(categoryId) && null != status && status > 0) {
			return articleRepository.selectArticlePages(categoryId, status, pageRequest);
		} else if (null != status && status > 0) {
			return articleRepository.selectArticlePageByStatus(status, pageRequest);
		} else if (!StringUtils.isEmpty(categoryId)) {
			return articleRepository.selectArticlePagesByCategory(categoryId, pageRequest);
		}
		return articleRepository.selectArticlePages(pageRequest);
	}

	public Article selectArticleDetail(String articleId) {
		return articleRepository.selectByUuid(articleId);
	}

	public Article saveArticle(Article article) {
		articleRepository.save(article);
		return article;
	}

	@Transactional
	public boolean deleteArticles(String[] ids) {
		for (String id : ids) {
			articleRepository.updateStatus(id, 0);
		}
		return true;
	}

	@Transactional
	public void addviewCount(String articleId, int count) {
		articleRepository.updateViewCount(articleId, count);
	}

	@Transactional
	public Integer delCategory(String categoryOld) {
		return articleRepository.delCategory(ArticleStatus.DRAFT.ordinal(), categoryOld);
	}
}
