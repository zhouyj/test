package com.feedss.portal.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.Pages;
import com.feedss.base.ip.IP;
import com.feedss.content.entity.Article;
import com.feedss.content.entity.Banner;
import com.feedss.content.entity.Category;
import com.feedss.content.entity.Banner.BannerLocation;
import com.feedss.content.service.BannerService;
import com.feedss.content.service.CategoryService;
import com.feedss.portal.service.NavService;
import com.feedss.portal.service.ShopService;
import com.feedss.portal.util.Constants;
import com.feedss.user.entity.UsrProduct;
import com.feedss.user.service.AccountService;
import com.feedss.user.service.UserProductService;
import com.feedss.user.service.UsrProductService;

import lombok.extern.slf4j.Slf4j;

/**
 * 商品兑换页面
 * 
 * @author Looly
 *
 */
@Slf4j
@Controller
@RequestMapping("/shop")
public class ShopController {

	private static final int DEFAUL_PAGE_SIZE = 20;

	@Autowired
	private ShopService shopService;
	@Autowired
	private UserProductService userProductService;
	@Autowired
	private UsrProductService usrProductService;

	@Autowired
	private AccountService accountService;
	@Autowired
	private NavService navService;
	@Autowired
	private BannerService bannerService;
	@Autowired
	private CategoryService categoryService;

	/**
	 * 积分商城
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "", method = { RequestMethod.GET, RequestMethod.POST })
	public String shop(HttpSession session, Map<String, Object> model) {
		if (null != session.getAttribute("user")) {
			JSONObject userJSON = (JSONObject) session.getAttribute(Constants.USER_SESSION);
			model.put("balance", accountService.getAccountInfo(userJSON.getString("uuid")));
		}
		model.put("navTree", navService.navTree()); // 导航
		model.put("shopNavTree", navService.shopNavTree()); // 商品导航

		// 首页商品，只有8个
		Pages<UsrProduct> productList = usrProductService.selectProductList(null, 0, 8, Article.ArticleStatus.PUBLISHED.ordinal());
		model.put("productList", productList); // 商品导航
		
		// 广告位
		JSONArray banners = bannerService.selectBannerGropyBy(BannerLocation.ShopPage.name());
		model.put("banners", banners);
		
		//顶部广告位
		List<Banner> topBanners = bannerService.selectBannerByLocation(BannerLocation.ShopTop.name());
		model.put("topBanners", topBanners);
		
		//左边广告位
		List<Banner> leftBanners = bannerService.selectBannerByLocation(BannerLocation.ShopLeft.name());
		if(null != leftBanners && false == leftBanners.isEmpty()){
			model.put("leftBanner", leftBanners.get(0));//左边只取一个广告
		}
		
		//顶部广告位
		List<Banner> middleBanners = bannerService.selectBannerByLocation(BannerLocation.ShopMiddle.name());
		model.put("middleBanners", middleBanners);

		return "/pufa/shop";
	}

	/**
	 * 积分商城商品列表
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.GET, RequestMethod.POST })
	public String list(HttpSession session, Map<String, Object> model, @RequestParam(name = "category", required = false) String category, @RequestParam(name = "pageNo", required = false, defaultValue = "0") int pageNo) {
		if (null != session.getAttribute("user")) {
			JSONObject userJSON = (JSONObject) session.getAttribute(Constants.USER_SESSION);
			model.put("balance", accountService.getAccountInfo(userJSON.getString("uuid")));
		}
		model.put("navTree", navService.navTree()); // 导航
		model.put("shopNavTree", navService.shopNavTree()); // 商品导航

		if (pageNo < 1) {
			pageNo = 0;
		}

		Pages<UsrProduct> productList;
		if (StringUtils.isBlank(category)) {
			productList = new Pages<>(0, new ArrayList<>());
		} else {
			List<Category> cList = categoryService.selectByParent(category, false);
			List<String> ids = new ArrayList<>();
			ids.add(category);
			for (Category c : cList) {
				ids.add(c.getUuid());
			}
			productList = usrProductService.selectProductListByCategoryList(ids, pageNo, DEFAUL_PAGE_SIZE, Article.ArticleStatus.PUBLISHED.ordinal());
		}
		model.put("list", productList);

		return "/pufa/shop_list";
	}

	/**
	 * 广告按照地域定位列表
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/bannerDetail", method = { RequestMethod.GET, RequestMethod.POST })
	public String bannerDetail(HttpSession session, HttpServletRequest request, Map<String, Object> model) {
		if (null != session.getAttribute("user")) {
			JSONObject userJSON = (JSONObject) session.getAttribute(Constants.USER_SESSION);
			model.put("balance", accountService.getAccountInfo(userJSON.getString("uuid")));
		}
		String category = request.getParameter("category");
		
		model.put("navTree", navService.navTree()); // 导航
		model.put("shopNavTree", navService.shopNavTree()); // 商品导航
		
		LinkedHashSet<Banner> bannerSet = new LinkedHashSet<>();
		// 地域
		String ip = IP.getClientIP(request);
		if (StringUtils.isNotBlank(ip)) {
			String[] citys = IP.find(ip);
			model.put("citys", citys);
			for (String city : citys) {
				if(StringUtils.isBlank(city)){
					continue;
				}
				List<Banner> list = bannerService.selectBannerLike(category, city);
				log.debug("City: {} result: {}", city, list.size());
				bannerSet.addAll(list);
			}
		}
		model.put("bannerList", new ArrayList<>(bannerSet));
		
		//分类对象
		Category categoryObj = categoryService.selectCategory(category);
		model.put("category", categoryObj);

		return "/pufa/shop_banner_detail";
	}

	/**
	 * 积分商城我的兑换列表
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/my", method = { RequestMethod.GET, RequestMethod.POST })
	public String my(HttpSession session, Map<String, Object> model) {
		if (null == session.getAttribute("user")) {
			return "redirect:/sign";// 未登录用户
		}
		JSONObject userJSON = (JSONObject) session.getAttribute(Constants.USER_SESSION);

		model.put("balance", accountService.getAccountInfo(userJSON.getString("uuid")));
		model.put("navTree", navService.navTree()); // 导航

		// 我的订单
		List<HashMap<String, Object>> productVos = userProductService.getProductsByUserId(userJSON.getString("uuid"));
		model.put("list", productVos);

		return "/pufa/shop_my";
	}

	/**
	 * 积分商城兑换页面
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/buy", method = { RequestMethod.GET, RequestMethod.POST })
	public String buy(HttpSession session, @RequestParam(name = "productId", required = false) String productId, Map<String, Object> model) {
		if (null == session.getAttribute("user")) {
			return "redirect:/sign";// 未登录用户
		}
		if (StringUtils.isBlank(productId)) {
			return "redirect:/";// 无商品编号
		}

		JSONObject userJSON = (JSONObject) session.getAttribute(Constants.USER_SESSION);
		model.put("balance", accountService.getAccountInfo(userJSON.getString("uuid")));
		model.put("product", usrProductService.getProductById(productId));
		model.put("navTree", navService.navTree()); // 导航

		return "/pufa/shop_buy";
	}

	/**
	 * 购买
	 * 
	 * @param session
	 * @param productId
	 * @return
	 */
	@RequestMapping(value = "doBuy", method = { RequestMethod.POST })
	@ResponseBody
	public JSONObject doBuy(HttpSession session, @RequestParam("productId") String productId) {
		JSONObject result = new JSONObject();
		JSONObject user = (JSONObject) session.getAttribute(Constants.USER_SESSION);
		if (null == user) {
			result.put("code", -1);
			result.put("msg", "当前用户未登录！");
			return result;
		}

		ErrorCode resultCode = shopService.buy(user.getString("uuid"), productId, 1);
		if (ErrorCode.SUCCESS == resultCode) {
			result.put("code", ErrorCode.SUCCESS.getCode());
			result.put("msg", "兑换成功。");
			return result;
		} else {
			result.put("code", resultCode.getCode());
			result.put("msg", resultCode.getMsg());
			return result;
		}
	}
}
