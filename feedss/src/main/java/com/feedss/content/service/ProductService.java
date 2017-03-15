package com.feedss.content.service;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.feedss.base.Constants;
import com.feedss.base.Pages;
import com.feedss.base.util.ConvertUtil;
import com.feedss.base.util.RestTemplateUtil;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.content.core.UserClientHttpRequestInterceptor;
import com.feedss.content.entity.Product;
import com.feedss.content.repository.ProductRepository;

/**
 * 商品<br>
 * 
 * @author wangjingqing
 * @date 2016-07-20
 */
@Component
public class ProductService {

	Log logger = LogFactory.getLog(getClass());

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ConfigureUtil configureUtil;

	public Product get(String productId){
		return productRepository.findOne(productId);
	}
	
	public List<Product> getAll(){
		return productRepository.findAll();
	}
	
	/**
	 * 根据类型Id查询信息<br>
	 * 
	 * @param typeIds
	 * @return List<Advert>
	 */
	public List<Product> selectProductList(List<String> productIds, String userId) {
		if (productIds == null || productIds.size() < 1) {
			return new ArrayList<Product>();
		}
		return productRepository.selectProductInProductId(productIds, userId);
	}
	
	public void deleteOldProduct(String userId){
		productRepository.updateStatusNotInProductId(userId);
	}

	public List<Product> findAll(List<String> productIds) {
		return productRepository.findAll(productIds);
	}

	/**
	 * 分页查询商品<br>
	 * 
	 * @param userId
	 * @param pageNo
	 * @param pageSize
	 */
	public Pages<Product> selectProductList(String userId, Integer pageNo, Integer pageSize) {
		Integer count = null;
		if(StringUtils.isEmpty(userId)){
			count = productRepository.selectCount();
		}else{
			count = productRepository.selectCountByUserId(userId);
		}
		
		List<Product> list = new ArrayList<Product>();
		// step:1 判断是否存在
		if (count > 0) {
			Sort sort = new Sort(Direction.DESC, "updated");// 排序
			PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);// 分页查询
			Page<Product> page = null;
			if(userId==null){
				page = productRepository.selectProductList(pageRequest);
			}else{
				page = productRepository.selectProductList(userId, pageRequest);
			}
			
			list = page.getContent();
		}
		return new Pages<Product>(count, list);
	}

	/**
	 * 拉取商品列表<br>
	 * 
	 * @param groupId
	 */
	public List<Product> pullProduct(String userId, Integer pageNo, Integer pageCount, String access_token) {
		StringBuilder para = new StringBuilder("pageCount=").append(pageCount).append("&pageNo=").append(pageNo)
				.append("&action=goodslist&access_token=").append(access_token);
		String info = RestTemplateUtil.postRequest(configureUtil.getConfig(Constants.MALL_DOMAIN), para.toString(),
				new UserClientHttpRequestInterceptor(access_token, MediaType.APPLICATION_FORM_URLENCODED_VALUE));
		if (info == null) {
			logger.info("拉取商品失败:access_token->" + access_token + " \n a->" + access_token);
			return new ArrayList<Product>();
		}
		String productInfo = new String(info.getBytes(Charset.forName("ISO-8859-1")));// 编码
		if (productInfo.contains("401 Unauthorized")) {
			return null;
		}
		List<Product> products = new ArrayList<Product>(); // 返回的列表
		try {
			Map<String, Object> infoMap = JSONObject.parseObject(productInfo, new TypeReference<Map<String, Object>>() {
			});
			Object list = infoMap.get("list");
			if (list != null) {
				List<Map<String, Object>> productList = JSONObject.parseObject(list.toString(),
						new TypeReference<List<Map<String, Object>>>() {
						});
				for (Map<String, Object> infoTmp : productList) {
					Product tempAdvert = new Product();
					tempAdvert.setCreated(new Date());
					tempAdvert.setUpdated(new Date());
					tempAdvert.setName(ConvertUtil.objectToString(infoTmp.get("goods_name"), true));
					tempAdvert.setProductId(ConvertUtil.objectToString(infoTmp.get("goods_id"), true));
					tempAdvert.setPrice(infoTmp.get("price") == null ? BigDecimal.ZERO
							: new BigDecimal(infoTmp.get("price").toString()));
					tempAdvert.setPicUrl(ConvertUtil.objectToString(infoTmp.get("default_image"), true));
					tempAdvert.setStatus(ConvertUtil.objectToInt(infoTmp.get("if_show"), false));
					tempAdvert.setCreator(userId);
					tempAdvert.setStocks(ConvertUtil.objectToInt(infoTmp.get("stock"), false));
					products.add(tempAdvert);
				}
			}
		} catch (Exception e) {
			logger.info("转换失败:", e);
			return products;
		}
		return products;
	}

	/**
	 * 批量存储Product<br>
	 * 
	 * @param Product
	 * @return
	 */
	public List<Product> save(List<Product> products) {
		return productRepository.save(products);
	}
}
