package com.feedss.manage.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.content.entity.Banner;
import com.feedss.content.entity.Category;
import com.feedss.content.entity.Category.CategoryType;
import com.feedss.content.service.BannerService;
import com.feedss.content.service.CategoryService;

/**
 * banner<br>
 * 
 * @author wangjingqing
 * @date 2016-08-20
 */
@Controller
@RequestMapping("/manage/banner")
public class ManageBannerController {

	@Autowired
	private BannerService bannerService;
	@Autowired
	private CategoryService categoryService;

	/**
	 * 查询banner列表<br>
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String list(HttpServletRequest request, HttpSession session, Model model) {
		int count = 0;
		List<Banner> banners = bannerService.selectBanner();
		if (banners != null)
			count = banners.size();
		model.addAttribute("banners", banners);
		model.addAttribute("bannerCount", count);
		return "/manage/banner/list";
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
		String bannerId = request.getParameter("bannerId");
		Banner banner = new Banner();
		banner.setUuid(null);
		if (bannerId != null) {
			banner = bannerService.selectByUUID(bannerId);
		}
		
		List<Category> categories = categoryService.selectCategoryByType(CategoryType.AD);//广告分类
		model.addAttribute("categories", categories);
		model.addAttribute("banner", banner);
		return "/manage/banner/add";
	}

	/**
	 * 添加banner<br>
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Object post(HttpServletRequest request, HttpSession session, Model model) {
		String bannerId = request.getParameter("bannerId");
//		UserVo user = (UserVo)session.getAttribute(Constants.USER_SESSION);
//		List<BannerVo> list = manageBannerService.selectBannerList(user);
//		List<Banner> list = bannerService.selectBanner();
//		if (StringUtils.isEmpty(bannerId) && list != null && list.size() >= 6) {
//			return JsonResponse.fail(ErrorCode.BANNER_TOO_MANY);
//		}
		String title = request.getParameter("title");
		String content = request.getParameter("content");
		String picUrl = request.getParameter("picUrl");
		String type = request.getParameter("type");
		String sortStr = request.getParameter("sort");
		String location = request.getParameter("location");
		String category = request.getParameter("category");
		String region = request.getParameter("region");

		if(StringUtils.isEmpty(picUrl)){
			return JsonResponse.fail(ErrorCode.UPLOAD_IMAGE);
		}
		
		if(!type.equals("0") && !type.equals("5") && StringUtils.isEmpty(content)){
			return JsonResponse.fail(ErrorCode.REQUIRED_INFO);
		}

//		boolean isSuccess = bannerService.save(bannerId, title, content, picUrl, Integer.valueOf(type), location, Integer.valueOf(sort), user);
		Banner banner = null;
		if (StringUtils.isNotBlank(bannerId)) {
			banner = bannerService.selectByUUID(bannerId);
			if (banner == null) {
				return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS); // TODO
			}
		} else {
			banner = new Banner();
			Integer sort = bannerService.selectMaxSort();
			sort = sort == null ? 0 : sort;
			banner.setSort(sort + 1);

			if (StringUtils.isNotEmpty(sortStr) && StringUtils.isNumeric(sortStr)) {
				sort = Integer.valueOf(sortStr);
				banner.setSort(sort);
			}

			banner.setStart(new Date());
			banner.setPlayStart(new Date());
			banner.setPlayEnd(new Date());
			banner.setEnd(new Date());
			banner.setStatus(1);
			banner.setCreated(new Date());
		}
		banner.setPicUrl(picUrl);
		banner.setType(type);
		banner.setTitle(title);
		banner.setContent(content);
		banner.setLocation(location);
		banner.setCategory(category);//广告分类
		banner.setRegion(region);//地域
		Banner result = bannerService.save(banner);
		if (result!=null)
			return JsonResponse.success(new ArrayList<>());
		else
			return JsonResponse.fail(ErrorCode.INTERNAL_ERROR);
	}

	/**
	 * 删除banner<br>
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public Object delete(HttpServletRequest request, HttpSession session, Model model) {
		String bannerId = request.getParameter("bannerId");
		boolean status = bannerService.delete(bannerId);
		if (!status) {
			return JsonResponse.fail(ErrorCode.INTERNAL_ERROR);
		}
		return JsonResponse.success(new ArrayList<>());
	}

	@ResponseBody
	@RequestMapping(value = "/sort", method = RequestMethod.POST)
	public Object sort(HttpServletRequest request, HttpSession session, Model model) {
		String bannerId = request.getParameter("bannerId");
		String sort = request.getParameter("sort");
		if (StringUtils.isBlank(bannerId) || !StringUtils.isNumeric(sort)) {
			return JsonResponse.fail(ErrorCode.TITLE_IS_EMPTY);
		}
		boolean status = bannerService.updateSort(bannerId, sort);
		if (!status) {
			return JsonResponse.fail(ErrorCode.INTERNAL_ERROR);
		}
		return JsonResponse.success(new ArrayList<>());
	}
}
