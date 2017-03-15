package com.feedss.content.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.Pages;
import com.feedss.base.util.ConvertUtil;
import com.feedss.contact.entity.Message;
import com.feedss.contact.service.MessageService;
import com.feedss.content.entity.Product;
import com.feedss.content.entity.ReleaseProduct;
import com.feedss.content.entity.ReleaseProduct.ReleaseType;
import com.feedss.content.model.room.Room;
import com.feedss.content.service.FavoriteService;
import com.feedss.content.service.ProductService;
import com.feedss.content.service.ReleaseProductService;
import com.feedss.content.service.RoomService;

/**
 * 广告API
 * 
 * @author wangjingqing
 * @since 1.0.0
 * @date 2016-07-23
 */
@RestController
@RequestMapping("/advert")
public class AdvertController {

	Log logger = LogFactory.getLog(getClass());

	@Autowired
	private ProductService productService;
	@Autowired
	private ReleaseProductService releaseProductService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private RoomService roomService;
	@Autowired
	private FavoriteService favoriteService;

	/**
	 * 商品详情<br>
	 * 
	 */
	@ResponseBody
	@RequestMapping("/detail")
	public ResponseEntity<Object> detail(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String productId = body.getString("productId");

		if (StringUtils.isEmpty(productId)) {
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}
		// 查询商品列表
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Product p = productService.get(productId);
		if(p!=null){
			tmpMap.put("productId", p.getUuid());// 商品ID
			tmpMap.put("name", p.getName());// 商品名称
			tmpMap.put("price", ConvertUtil.bigDecimalToString(p.getPrice()));// 商品价格
			tmpMap.put("picUrl", p.getPicUrl());// 商品图片
			tmpMap.put("ext", p.getExtAttr());// 扩展信息
		}
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("product", tmpMap);
		return JsonResponse.success(reMap);

	}

	/**
	 * 商品列表<br>
	 * 
	 */
	@ResponseBody
	@RequestMapping("/all")
	public ResponseEntity<Object> all(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		Integer pageNo = body.getInteger("pageNo");
		pageNo = pageNo == null ? 0 : --pageNo;
		Integer pageSize = body.getInteger("pageSize");
		pageSize = pageSize == null ? 10 : pageSize;

		// 查询商品列表
		Pages<Product> products = productService.selectProductList(null, pageNo, pageSize);
		Integer totalCount = products.getTotalCount();// 总数量
		List<Product> productList = products.getList();
		List<Map<String, Object>> reList = new ArrayList<Map<String, Object>>();// 返回list
		if (productList.size() > 0) {
			for (Product product : productList) {
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("productId", product.getUuid());// 商品ID
				tmpMap.put("name", product.getName());// 商品名称
				tmpMap.put("price", ConvertUtil.bigDecimalToString(product.getPrice()));// 商品价格
				tmpMap.put("picUrl", product.getPicUrl());// 商品图片
				tmpMap.put("ext", product.getExtAttr());// 扩展信息
				reList.add(tmpMap);
			}
		}
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("list", reList);
		reMap.put("totalCount", totalCount);
		return JsonResponse.success(reMap);

	}

	/**
	 * 商品列表<br>
	 * 
	 */
	@ResponseBody
	@RequestMapping("/list")
	public ResponseEntity<Object> list(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = request.getHeader("userId");
		Integer pageNo = body.getInteger("pageNo");
		pageNo = pageNo == null ? 0 : --pageNo;
		Integer pageSize = body.getInteger("pageSize");
		pageSize = pageSize == null ? 10 : pageSize;
		Integer isPush = body.getInteger("isPush");// 是否刷新 0:否 1：是

		String streamId = body.getString("streamId");
		if (streamId == null) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		// step:1 拉取商品信息
		if (isPush.intValue() == 1) {
			String accessToken = body.getString("accessToken");// 三方认证
			if (StringUtils.isBlank(accessToken)) {
				return JsonResponse.fail(ErrorCode.PULL_PRODUCT_AUTHFAILURE);
			}
			logger.info("拉取商品信息:userId->" + userId + " \n accessToken->" + accessToken);
			int status = synchronizationProduct(userId, accessToken);// 拉取信息
			if (status == 0) {
				return JsonResponse.fail(ErrorCode.PULL_PRODUCT_AUTHFAILURE);// 认证失败
			}
		}
		// 查询商品列表
		Pages<Product> products = productService.selectProductList(userId, pageNo, pageSize);
		Integer totalCount = products.getTotalCount();// 总数量
		List<Product> productList = products.getList();
		// step:2 查询发布商品的
		List<String> productIds = new ArrayList<String>();
		for (Product product : productList) {
			productIds.add(product.getUuid());
		}
		Map<String, ReleaseProduct> advertMap = new HashMap<String, ReleaseProduct>();// 存放发布商品
		if (!productIds.isEmpty()) {
			List<ReleaseProduct> releaseProducts = releaseProductService.selectReleaseProduct(streamId, productIds);
			for (ReleaseProduct releaseProduct : releaseProducts) {
				advertMap.put(releaseProduct.getProductId(), releaseProduct);
			}
		}
		List<Map<String, Object>> reList = new ArrayList<Map<String, Object>>();// 返回list
		if (productList.size() > 0) {
			for (Product product : productList) {
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("productId", product.getUuid());// 商品ID
				tmpMap.put("name", product.getName());// 商品名称
				tmpMap.put("price", ConvertUtil.bigDecimalToString(product.getPrice()));// 商品价格
				tmpMap.put("picUrl", product.getPicUrl());// 商品图片
				tmpMap.put("isRelease", advertMap.get(product.getUuid()) == null ? 0 : 1);// 是否发布
																							// 0：否
																							// 1：是
				reList.add(tmpMap);
			}
		}
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("list", reList);
		reMap.put("totalCount", totalCount);
		return JsonResponse.success(reMap);
	}

	/**
	 * 发布商品API<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/publish", method = RequestMethod.POST)
	public ResponseEntity<Object> publish(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = request.getHeader("userId");
		String ids = body.getString("ids");
		if (StringUtils.isBlank(ids)) {
			return JsonResponse.fail(ErrorCode.CHOOSE_ONE_PRODUCT);// 请选着一款商品
		}
		String uuid = body.getString("streamId");// 视频Id
		if (StringUtils.isBlank(uuid)) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		String[] productIds = ids.split(",");

		// step:1 查询发布商品信息
		List<ReleaseProduct> releaseProducts = releaseProductService.selectReleaseProduct(uuid,
				Arrays.asList(productIds));
		Map<String, ReleaseProduct> releaseProductMap = new HashMap<String, ReleaseProduct>();// 存放已发布商品信息
		for (ReleaseProduct releaseProduct : releaseProducts) {
			releaseProductMap.put(releaseProduct.getProductId(), releaseProduct);
		}

		// step:2 查询商品信息
		List<Product> productList = productService.findAll(Arrays.asList(productIds));
		Map<String, Product> productMap = new HashMap<String, Product>();// 存放商品信息
		for (Product product : productList) {
			productMap.put(product.getUuid(), product);
		}
		// step:3 更新或报保存发布商品信息
		List<ReleaseProduct> saveReleaseProduct = new ArrayList<ReleaseProduct>();// 存储list
		for (int i = 0; i < productIds.length; i++) {
			String productId = productIds[i];
			Product product = productMap.get(productId);
			if (product == null) {
				continue;
			}
			ReleaseProduct releaseProduct = releaseProductMap.get(productId);
			if (releaseProduct == null) {
				releaseProduct = new ReleaseProduct();
				releaseProduct.setProductId(productId);
				releaseProduct.setStreamId(uuid);
				releaseProduct.setCreator(userId);
				releaseProduct.setCreated(new Date());
				releaseProduct.setType(ReleaseType.Product.name());
			}
			// 查询商品信息
			releaseProduct.setName(product.getName());
			releaseProduct.setPicUrl(product.getPicUrl());
			releaseProduct.setPrice(product.getPrice());
			releaseProduct.setUpdated(new Date());
			saveReleaseProduct.add(releaseProduct);
		}
		// step:4 保存发布商品信息
		try {
			releaseProductService.saveReleaseProduct(saveReleaseProduct);// 批量发布商品
		} catch (Exception e) {
			logger.info("发布商品异常", e);
			return JsonResponse.fail(ErrorCode.INTERNAL_FAILURE);
		}
		// step:5 推送消息到观众
		Room room = roomService.getRoom(uuid);
		if (room != null) {
			Map<String, Object> extInfo = new HashMap<String, Object>();// 扩展信息
			extInfo.put("groupId", room.getGroupId());
			extInfo.put("productList", saveReleaseProduct);
			extInfo.put("messageSource", Message.Source.Advertisement.name());
			// pushService.push(userId, room.getGroupId(),
			// JSONObject.toJSONString(extInfo));
			messageService.sendGroupMessage(room.getGroupId(), "", extInfo, null);
		}
		return JsonResponse.success();
	}

	/**
	 * 发布商品列表<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/releaseList", method = RequestMethod.POST)
	public ResponseEntity<Object> releaseList(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = request.getHeader("userId");
		String streamId = body.getString("streamId");
		if (StringUtils.isBlank(userId) || StringUtils.isBlank(streamId)) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		Integer pageNo = body.getInteger("pageNo");
		pageNo = pageNo == null ? 0 : --pageNo;
		Integer pageSize = body.getInteger("pageSize");
		pageSize = pageSize == null ? 10 : pageSize;

		Pages<ReleaseProduct> pages = releaseProductService.selectReleaseProductList(streamId, pageNo, pageSize);
		Integer totalCount = pages.getTotalCount();
		List<Map<String, Object>> reList = new ArrayList<Map<String, Object>>();
		List<ReleaseProduct> releaseProducts = pages.getList();
		if (releaseProducts.size() > 0) {
			List<String> productIds = new ArrayList<String>();// 获取商品ids
																// 用于批量查询是否加入购物车
			for (ReleaseProduct release : releaseProducts) {
				productIds.add(release.getProductId());
			}

			Map<String, Integer> isFav = favoriteService.isFavorited(productIds, userId);
			for (ReleaseProduct releaseProduct : releaseProducts) {
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("productId", releaseProduct.getProductId());// 商品ID
				tmpMap.put("name", ConvertUtil.objectToString(releaseProduct.getName(), false));// 商品名称
				tmpMap.put("price", ConvertUtil.bigDecimalToString(releaseProduct.getPrice()));// 商品价格
				// 查询是否收藏了该商品
				tmpMap.put("isCollection", isFav.get(releaseProduct.getProductId()));// 是否收藏
																						// 0：否
																						// 1：是
				tmpMap.put("picUrl", ConvertUtil.objectToString(releaseProduct.getPicUrl(), false));// 商品图片
				reList.add(tmpMap);
			}
		}
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("list", reList);
		data.put("totalCount", totalCount);
		return JsonResponse.success(data);
	}

	/**
	 * 获取真实商品ids<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/goodsIds", method = RequestMethod.POST)
	public ResponseEntity<Object> goodsIds(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String goodsIds = body.getString("goodsIds");
		String reGoodsId = "";
		if (StringUtils.isNotBlank(goodsIds)) {
			StringBuilder goodsId = new StringBuilder();
			String[] idArray = goodsIds.split(",");
			List<Product> products = productService.findAll(Arrays.asList(idArray));
			products.forEach((product) -> {
				goodsId.append("," + product.getProductId());
			});
			reGoodsId = goodsId.toString();
			if (reGoodsId.charAt(0) == ',') {
				reGoodsId = reGoodsId.substring(1);
			}
		}
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("goodsIds", reGoodsId);
		return JsonResponse.success(reMap);
	}

	/**
	 * 拉取<br>
	 * 
	 * @param userId
	 * @param access_token
	 * @return
	 */
	private int synchronizationProduct(String userId, String access_token) {
		List<Product> productList = productService.pullProduct(userId, 0, 100, access_token);// 拉取信息
		if (productList == null) {
			return 0;
		}
		if (productList.size() < 1) {
			return 1;
		}
		List<String> typeIds = new ArrayList<String>();// 存放商品id
		for (Product product : productList) {
			typeIds.add(product.getProductId());
		}
		// 将老数据都处理掉
		productService.deleteOldProduct(userId);

		List<Product> products = productService.selectProductList(typeIds, userId);// 查询是否存在已有的商品
		Map<String, Product> productMap = new HashMap<String, Product>();
		for (Product product : products) {
			productMap.put(product.getProductId(), product);
		}
		List<Product> saveList = new ArrayList<Product>();
		for (Product product : productList) {
			Product tmpProduct = productMap.get(product.getProductId());
			if (tmpProduct != null) {
				tmpProduct.setPicUrl(product.getPicUrl());
				tmpProduct.setName(product.getName());
				tmpProduct.setPrice(product.getPrice());
				tmpProduct.setStatus(product.getStatus());
				tmpProduct.setUpdated(new Date());
				tmpProduct.setStocks(product.getStocks());
			} else {
				tmpProduct = product;
			}
			saveList.add(tmpProduct);
		}
		// 更新商品信息
		productService.save(saveList);
		return 1;
	}
}
