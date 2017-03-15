package com.feedss.portal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.Pages;
import com.feedss.contact.service.MessageService;
import com.feedss.content.entity.Article;
import com.feedss.content.entity.Article.ArticleStatus;
import com.feedss.content.entity.Stream;
import com.feedss.content.service.ArticleService;
import com.feedss.content.service.BannerService;
import com.feedss.content.service.StreamService;
import com.feedss.portal.service.NavService;
import com.feedss.portal.util.Constants;
import com.feedss.user.entity.Favorite.FavoriteType;
import com.feedss.user.service.AccountService;
import com.feedss.user.service.UserFavoriteService;
import com.feedss.user.service.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * 51普法页面
 * 
 * @author Looly
 *
 */
@Slf4j
@Controller
@RequestMapping("")
public class HomePageController {

	private static final int DEFAUL_PAGE_SIZE = 20;

	@Autowired
	private NavService navService;

	@Autowired
	private BannerService bannerService;
	@Autowired
	private ArticleService articleService;
	@Autowired
	private StreamService streamService;
	@Autowired
	private UserService userService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private UserFavoriteService userFavoriteService;
	@Autowired
	private MessageService messageService;

	/**
	 * 主页
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/index")
	public String index(HttpSession session, Map<String, Object> model) {
		JSONObject userJson = (JSONObject) session.getAttribute("user");
		if (userJson != null) {
			String userId = userJson.getString("uuid");// 虚拟币
			model.put("balance", (JSONObject) JSON.toJSON(accountService.getAccountBalance(userId)));
		}

		// model.put("navTree", navService.navTree()); //导航
		// model.put("bannerList", remoteContentService.bannerList()); //广告
		// model.put("articlesGroupByCategory",
		// remoteContentService.articlesGroupByCategory()); //分类列表内容

		model.put("navTree", navService.navTree()); // 导航
		model.put("bannerList", bannerService.selectBanner());// 广告
		model.put("articlesGroupByCategory", articleService.listGroupByCategory(ArticleStatus.PUBLISHED.ordinal(), 3)); // 分类列表内容

		return "/pufa/index";
	}

	/**
	 * 列表页
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/list")
	public String list(HttpSession session, @RequestParam(name = "category", required = false) String category,
			@RequestParam(name = "pageNo", required = false, defaultValue = "1") int pageNo,
			Map<String, Object> model) {

		JSONObject userJson = (JSONObject) session.getAttribute("user");
		if (userJson != null) {
			String userId = userJson.getString("uuid");// 虚拟币
			model.put("balance", (JSONObject) JSON.toJSON(accountService.getAccountBalance(userId)));
		}

		model.put("navTree", navService.navTree()); // 导航
		model.put("bannerList", bannerService.selectBanner());// 广告
		model.put("articlesGroupByCategory", articleService.listGroupByCategory(ArticleStatus.PUBLISHED.ordinal(), 3)); // 分类列表内容
		model.put("breadNav", navService.breadNav(category));//面包屑导航

		if (pageNo < 1) {
			pageNo = 1;
		}
		// JSONObject articleList = remoteContentService.articleList(category,
		// pageNo, DEFAUL_PAGE_SIZE);
		JSONObject result = new JSONObject();
		Page<Article> articlePage = articleService.selectArticlePages(category, ArticleStatus.PUBLISHED.ordinal(), pageNo-1, DEFAUL_PAGE_SIZE);
		result.put("list", articlePage.getContent());
		result.put("pageNo", pageNo);
		result.put("totalPage", articlePage.getTotalPages());
		result.put("category", category);

		model.put("data", result);

		return "/pufa/list";
	}

	/**
	 * 直播列表页
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/lives")
	public String lives(HttpSession session,
			// @RequestParam(name="category", required=false)
			@RequestParam(name = "pageNo", required = false, defaultValue = "0") int pageNo,
			Map<String, Object> model) {

		JSONObject userJson = (JSONObject) session.getAttribute("user");
		if (userJson != null) {
			String userId = userJson.getString("uuid");// 虚拟币
			model.put("balance", (JSONObject) JSON.toJSON(accountService.getAccountBalance(userId)));
		}

		model.put("navTree", navService.navTree()); // 导航
		model.put("bannerList", bannerService.selectBanner());// 广告
		model.put("articlesGroupByCategory", articleService.listGroupByCategory(ArticleStatus.PUBLISHED.ordinal(), 3)); // 分类列表内容

		if (pageNo < 0) {
			pageNo = 0;
		}
		// JSONObject articleList = remoteContentService.liveList(pageNo,
		// DEFAUL_PAGE_SIZE);
		JSONObject result = new JSONObject();
		Pages<Stream> pages = streamService.findStreams(-1, null, null, null, pageNo, DEFAUL_PAGE_SIZE);
		Integer totalCount = pages.getTotalCount();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (totalCount > 0) {
			List<Stream> streamList = pages.getList();
			list = streamService.getStreams(streamList, false);
		}
		result.put("list", list);
		result.put("pageNo", pageNo);
		int totalPage = (totalCount % DEFAUL_PAGE_SIZE == 0) ? (totalCount / DEFAUL_PAGE_SIZE)
				: (totalCount / DEFAUL_PAGE_SIZE + 1);
		result.put("totalPage", totalPage);

		model.put("data", result);

		return "/pufa/lives";
	}

	@RequestMapping(value = "/livePlay")
	public String livePlay(HttpSession session, @RequestParam(name = "uuid", required = false) String uuid, Map<String, Object> model) {

		JSONObject userJson = (JSONObject) session.getAttribute("user");
		if (userJson != null) {
			String userId = userJson.getString("uuid");// 虚拟币
			model.put("balance", (JSONObject) JSON.toJSON(accountService.getAccountBalance(userId)));
		}

		model.put("navTree", navService.navTree()); // 导航
		model.put("bannerList", bannerService.selectBanner());// 广告
		model.put("articlesGroupByCategory", articleService.listGroupByCategory(ArticleStatus.PUBLISHED.ordinal(), 3)); // 分类列表内容

		if(StringUtils.isNotBlank(uuid)){
			Stream stream = streamService.selectStream(uuid);
			model.put("stream", stream);
		}

		return "/pufa/live_play";
	}

	/**
	 * 详情页
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/article")
	public String artile(HttpSession session, @RequestParam(name = "articleId", required = false) String articleId,
			Map<String, Object> model) {

		// 虚拟币
		// if(session.getAttribute(Constants.USER_SESSION) != null){
		// model.put("balance", remoteUserService.getBalance());
		// }

		model.put("navTree", navService.navTree()); // 导航
		model.put("bannerList", bannerService.selectBanner());// 广告
		model.put("articlesGroupByCategory", articleService.listGroupByCategory(ArticleStatus.PUBLISHED.ordinal(), 3)); // 分类列表内容

		// JSONObject articleDetail =
		// remoteContentService.articleDetail(articleId);
		String userId = null;
		JSONObject user = (JSONObject) session.getAttribute(Constants.USER_SESSION);
		if (user != null) {
			userId = user.getString("uuid");
		}
		Article article = articleService.viewArticle(articleId, "web", userId);
		model.put("data", article);
		// 是否已收藏
		if (session.getAttribute(Constants.USER_SESSION) != null) {
			model.put("isFavorite", userFavoriteService.isExist(userId, "article", articleId, FavoriteType.FAVORITE.name()) ? 1 : 0);
		}
		
		//面包屑导航
		model.put("breadNav", navService.breadNav(article.getCategory()));

		return "/pufa/article";
	}

	/**
	 * 登录注册页
	 * 
	 * @param session
	 * @param from
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/sign", method = { RequestMethod.GET, RequestMethod.POST })
	public String sign(HttpSession session, @RequestParam(value = "from", required = false) String from,
			Map<String, Object> model) {
		model.put("navTree", navService.navTree()); // 导航

		if (StringUtils.isNotBlank(from)) {
			return "redirect:" + from;
		}

		if (null != session.getAttribute("user")) {
			// 登录用户直接到个人主页
			return "redirect:/profile";
		}

		return "/pufa/sign";
	}

	/**
	 * 注册页
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/signupByUserPwd", method = { RequestMethod.GET, RequestMethod.POST })
	public String signupByUserPwd(HttpSession session, Map<String, Object> model) {
		model.put("navTree", navService.navTree()); // 导航

		// 检查session中的点卡
		String serialNumber = (String) session.getAttribute("serialNumber");
		if (StringUtils.isBlank(serialNumber)) {
			// 拒绝未验证点卡的请求
			return "redirect:/sign";
		}

		return "/pufa/signup_userpwd";
	}

	/**
	 * 重置密码页面
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/resetPwdPage", method = { RequestMethod.GET, RequestMethod.POST })
	public String resetPwdPage(HttpSession session, Map<String, Object> model) {
		model.put("navTree", navService.navTree()); // 导航

		return "/pufa/reset_pwd";
	}

	/**
	 * 登出
	 * 
	 * @param session
	 * @param from
	 */
	@RequestMapping(value = "/signout", method = { RequestMethod.GET, RequestMethod.POST })
	public String signout(HttpSession session, @RequestParam(value = "from", required = false) String from) {
		session.removeAttribute("user");
		return "redirect:" + (StringUtils.isBlank(from) ? "/" : from);
	}

	/**
	 * 完善个人信息页
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/improveInfoPage", method = { RequestMethod.GET, RequestMethod.POST })
	public String improveInfoPage(HttpSession session, Map<String, Object> model) {
		model.put("navTree", navService.navTree()); // 导航
		if (null == session.getAttribute("user")) {
			return "redirect:/sign";// 未登录用户
		}
		JSONObject userJson = (JSONObject) session.getAttribute("user");
		if (userJson != null) {
			String userId = userJson.getString("uuid");// 虚拟币
			model.put("balance", (JSONObject) JSON.toJSON(accountService.getAccountBalance(userId)));
		}
		model.put("navTree", navService.navTree()); // 导航

		return "/pufa/improve_info";
	}

	/**
	 * 注册结束
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/signupFinish", method = { RequestMethod.GET, RequestMethod.POST })
	public String signupFinish(HttpSession session, Map<String, Object> model) {
		if (null == session.getAttribute("user")) {
			return "redirect:/sign";// 未登录用户
		}
		if (null == session.getAttribute("reward")) {
			session.setAttribute("reward", 0);
		}
		JSONObject userJson = (JSONObject) session.getAttribute("user");
		if (userJson != null) {
			String userId = userJson.getString("uuid");// 虚拟币
			model.put("balance", (JSONObject) JSON.toJSON(accountService.getAccountBalance(userId)));
		}

		model.put("navTree", navService.navTree()); // 导航
		return "/pufa/signup_finish";
	}

	/**
	 * 个人中心
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/profile", method = { RequestMethod.GET, RequestMethod.POST })
	public String profile(HttpSession session, Map<String, Object> model) {
		model.put("navTree", navService.navTree()); // 导航
		if (null == session.getAttribute("user")) {
			return "redirect:/sign";// 未登录用户
		}

		JSONObject userJSON = (JSONObject) session.getAttribute("user");
		String userId = userJSON.getString("uuid");
		// profile
		Map<String, Object> result = userService.getProfile(userId, userId);
		if (null != result) {
			model.put("user", result.get("user"));
		}
		model.put("balance", (JSONObject) JSON.toJSON(accountService.getAccountBalance(userId)));

		// 消息
//		model.put("messages", remoteConnectService.getMsg(1, 20));
		model.put("messages", messageService.getSystemMessage(1, 20, null));

		// 收藏

		model.put("favorites", getFavorites(userId, FavoriteType.FAVORITE.name()));

		// 足迹
		model.put("views", getFavorites(userId, FavoriteType.VIEW.name()));

		return "/pufa/profile";
	}

	JSONObject getFavorites(String userId, String type) {
		JSONObject favorites = (JSONObject) JSON
				.toJSON(userFavoriteService.getFavorites(userId, 20, null, null, type, null));
		if (favorites.getIntValue("code") == 0) {
			JSONObject data = favorites.getJSONObject("data");
			if (null != data && false == data.isEmpty()) {
				JSONArray list = data.getJSONArray("list");
				if (list != null && false == list.isEmpty()) {
					JSONObject item;
					for (int i = 0; i < list.size(); i++) {
						item = list.getJSONObject(i);
						if (null != item) {
							String extJsonStr = item.getString("extAttr");
							if (StringUtils.isNotBlank(extJsonStr)) {
								try {
									item.put("extAttr", JSON.parse(extJsonStr));
								} catch (Exception e) {
									log.warn("Parse {} error, item: {}", extJsonStr, item);
								}
							}
						}
					}
				}
			}
		}
		return favorites;
	}

	/**
	 * 绑定卡片页面
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/bindCardPage", method = { RequestMethod.GET, RequestMethod.POST })
	public String bindCardPage(HttpSession session, Map<String, Object> model) {
		if (null == session.getAttribute("user")) {
			return "redirect:/sign";// 未登录用户
		}

		JSONObject userJson = (JSONObject) session.getAttribute("user");
		if (userJson != null) {
			String userId = userJson.getString("uuid");// 虚拟币
			model.put("balance", (JSONObject) JSON.toJSON(accountService.getAccountBalance(userId)));
		}
		model.put("navTree", navService.navTree()); // 导航

		return "/pufa/bind_card";
	}

	/**
	 * 绑定或重新绑定手机页面
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/bindMobilePage", method = { RequestMethod.GET, RequestMethod.POST })
	public String bindMobilePage(HttpSession session, Map<String, Object> model) {
		if (null == session.getAttribute("user")) {
			return "redirect:/sign";// 未登录用户
		}

		JSONObject userJson = (JSONObject) session.getAttribute("user");
		if (userJson != null) {
			String userId = userJson.getString("uuid");// 虚拟币
			model.put("balance", (JSONObject) JSON.toJSON(accountService.getAccountBalance(userId)));
		}
		model.put("navTree", navService.navTree()); // 导航
		return "/pufa/bind_mobile";
	}

	/**
	 * 修改密码页面
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/changePwdPage", method = { RequestMethod.GET, RequestMethod.POST })
	public String changePwdPage(HttpSession session, Map<String, Object> model) {
		if (null == session.getAttribute("user")) {
			return "redirect:/sign";// 未登录用户
		}
		JSONObject userJson = (JSONObject) session.getAttribute("user");
		if (userJson != null) {
			String userId = userJson.getString("uuid");// 虚拟币
			model.put("balance", (JSONObject) JSON.toJSON(accountService.getAccountBalance(userId)));
		}
		model.put("navTree", navService.navTree()); // 导航
		return "/pufa/change_pwd";
	}

	/**
	 * 编辑个人信息页面，从session中回填
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/editInfoPage", method = { RequestMethod.GET, RequestMethod.POST })
	public String editInfoPage(HttpSession session, Map<String, Object> model) {
		if (null == session.getAttribute("user")) {
			return "redirect:/sign";// 未登录用户
		}

		JSONObject userJson = (JSONObject) session.getAttribute("user");
		if (userJson != null) {
			String userId = userJson.getString("uuid");// 虚拟币
			model.put("balance", (JSONObject) JSON.toJSON(accountService.getAccountBalance(userId)));
		}
		model.put("navTree", navService.navTree()); // 导航
		return "/pufa/edit_info";
	}
}
