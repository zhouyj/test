package com.feedss.manage.controller;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.Pages;
import com.feedss.content.entity.Article;
import com.feedss.content.entity.Category;
import com.feedss.content.entity.Category.CategoryType;
import com.feedss.content.service.CategoryService;
import com.feedss.manage.util.Constants;
import com.feedss.user.entity.UserProduct;
import com.feedss.user.entity.UsrProduct;
import com.feedss.user.model.UserVo;
import com.feedss.user.repository.UserProductRepository;
import com.feedss.user.repository.UsrProductRepository;
import com.feedss.user.service.UsrProductService;

/**
 * 商品管理页面
 */
@Controller
@RequestMapping("/manage/product")
public class ManageProductController {

	@Autowired
	private UsrProductService usrProductService;
	@Autowired
	private UsrProductRepository usrProductRepo;
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private UserProductRepository userProductRepository;
	
	/**
	 * 查询usr_product列表<br>
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/", method = RequestMethod.GET)
	public String list(HttpServletRequest request,HttpSession session, Model model){
		Integer status = request.getParameter("status") == null ? 2 : Integer.valueOf(request.getParameter("status"));
		Integer pageNo = request.getParameter("pageNo") == null ? 0 : Integer.valueOf(request.getParameter("pageNo"));
		Integer pageSize = request.getParameter("pageSize") == null ? 50 : Integer.valueOf(request.getParameter("pageSize"));
		String category = request.getParameter("category");

		UserVo userVo = (UserVo)session.getAttribute(Constants.USER_SESSION);
		List<Category> categories = categoryService.selectCategoryByType(CategoryType.Shop);
		Pages<UsrProduct> usrProductPages;
		if(StringUtils.isNotBlank(category)){
			usrProductPages = usrProductService.selectProductList(category, pageNo, pageSize, null);
		}else{
			Sort sort = new Sort(Direction.DESC, "updated");// 排序
			PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);// 分页查询
			Page<UsrProduct> page = usrProductRepo.findAll(status, pageRequest);
			usrProductPages = new Pages<UsrProduct>((int) page.getTotalElements(), page.getContent());
		}
		
		model.addAttribute("categoryList", categories);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("articles", usrProductPages.getList());
		model.addAttribute("totalCount", usrProductPages.getTotalCount());
		model.addAttribute("pageCount", usrProductPages.getPageCount());
		model.addAttribute("status",status);
		model.addAttribute("pageNo", pageNo);
		model.addAttribute("userId", userVo.getUuid());
		return "/manage/product/list";
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
		String usrProductId = request.getParameter("usrProductId");

		UserVo user = (UserVo)session.getAttribute(Constants.USER_SESSION);
		UsrProduct usrProduct = null;
		if (!StringUtils.isEmpty(usrProductId)){
			usrProduct = usrProductService.getProductById(usrProductId);
		}

		if (null == usrProduct){
			usrProduct = new UsrProduct();
		}
		model.addAttribute("userId", user.getUuid());
		List<Category> categories = categoryService.selectCategoryByType(CategoryType.Shop);
		model.addAttribute("categories", categories);
		model.addAttribute("usrProduct", usrProduct);

		return "/manage/product/add";
	}

	/**
	 * 添加商品
	 *
	 * * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public ResponseEntity<Object> add(HttpServletRequest request,HttpSession session) {
		String productId = request.getParameter("productId");
		if(StringUtils.isBlank(productId)){
			productId = UUID.randomUUID().toString();
		}
		String title = request.getParameter("title");
		String tags = request.getParameter("tags");
		String category = request.getParameter("category");
		String content = request.getParameter("content");
		String cover = request.getParameter("cover");
		int price = Integer.parseInt(request.getParameter("price"));
		int stocks = Integer.parseInt(request.getParameter("stocks"));//库存
		
		UsrProduct usrProduct = new UsrProduct();
		usrProduct.setUuid(productId);
		usrProduct.setName(title);
		usrProduct.setTags(tags);
		usrProduct.setCategory(category);
		usrProduct.setDescription(content);
		usrProduct.setPic(cover);
		usrProduct.setPrice(price);
		usrProduct.setRank(1);
		usrProduct.setStatus(Article.ArticleStatus.PUBLISHED.ordinal());
		usrProduct.setType(UsrProduct.ProductType.NORMAL.name());
		usrProduct.setStocks(stocks);
		
		usrProduct.setUpdated(new Date());
		UserVo userVo = (UserVo)session.getAttribute(Constants.USER_SESSION);
		usrProduct.setUpdater(request.getHeader(userVo.getUuid()));
		
		UsrProduct result = usrProductRepo.save(usrProduct);
		return result==null ? JsonResponse.fail(ErrorCode.CHECK_PASSWORD_ERROR) : JsonResponse.success(new JSONObject());
	}

	/**
	 * 删除商品
	 *
	 * * @return
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public ResponseEntity<Object> delete(HttpServletRequest request,HttpSession session) {
		String ids = request.getParameter("ids");
		if (StringUtils.isEmpty(ids)){
			return JsonResponse.fail(ErrorCode.CHECK_PASSWORD_ERROR);
		}
		boolean result  =usrProductService.deleteUsrProducts(ids.split(","));
		return result ? JsonResponse.success(new JSONObject()) : JsonResponse.fail(ErrorCode.CHECK_PASSWORD_ERROR);
	}
	
	/**
	 * 查询usr_product列表<br>
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/userProductList", method = RequestMethod.GET)
	public String userProductList(HttpServletRequest request,HttpSession session, Model model){
		Integer status = request.getParameter("status") == null ? 1 : Integer.valueOf(request.getParameter("status"));
		Integer pageNo = request.getParameter("pageNo") == null ? 0 : Integer.valueOf(request.getParameter("pageNo"));
		Integer pageSize = request.getParameter("pageSize") == null ? 50 : Integer.valueOf(request.getParameter("pageSize"));

		Pages<UserProduct> userProductPages;
		Sort sort = new Sort(Direction.DESC, "updated");// 排序
		PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);// 分页查询
		Page<UserProduct> page = userProductRepository.getProductsByStatus(status, pageRequest);
		userProductPages = new Pages<UserProduct>((int) page.getTotalElements(), page.getContent());
		
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("userProducts", userProductPages.getList());
		model.addAttribute("totalCount", userProductPages.getTotalCount());
		model.addAttribute("pageCount", userProductPages.getPageCount());
		model.addAttribute("status",status);
		model.addAttribute("pageNo", pageNo);
		return "/manage/product/userProductList";
	}
}
