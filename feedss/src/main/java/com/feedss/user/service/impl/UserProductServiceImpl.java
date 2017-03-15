package com.feedss.user.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feedss.user.entity.UserProduct;
import com.feedss.user.entity.UsrProduct;
import com.feedss.user.model.ProductVo;
import com.feedss.user.repository.UserProductRepository;
import com.feedss.user.repository.UsrProductRepository;
import com.feedss.user.service.UserProductService;

/**
 * Created by qin.qiang on 2016/8/4 0004.
 */
@Service
public class UserProductServiceImpl implements UserProductService {

	@Autowired
	private UsrProductRepository productRepository;

	@Autowired
	private UserProductRepository userProductRepository;
	
	@Override
	public List<HashMap<String, Object>> getProductsByUserId(String userId) {
		List<UserProduct> userProducts = userProductRepository.getProductsByUserId(userId);
		List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		for (UserProduct userProduct : userProducts) {
			HashMap<String, Object> hashMap = new HashMap<String, Object>();
			hashMap.put("userProductId", userProduct.getUuid());
			int num = userProduct.getProductNum();
			UsrProduct product = productRepository.findOne(userProduct.getProductId());
			hashMap.put("productNum", num);
			ProductVo productVo = ProductVo.product2Vo(product);
			productVo.setStatus(userProduct.getStatus()); // 订单状态：
			hashMap.put("product", productVo);
			data.add(hashMap);
		}
		Collections.sort(data, new Comparator<HashMap<String, Object>>(){
			@Override
			public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
				ProductVo productVo1 = (ProductVo) o1.get("product");
				ProductVo productVo2 = (ProductVo) o2.get("product");
				return productVo2.getPrice() > productVo1.getPrice() ? 1 : -1;
			}
		});
		return data;
	}

	@Override
	public long getProductsSumByUserId(String userId) {
		Long num = userProductRepository.getSumByUserId(userId);
		return num == null ? 0 : num;
	}
}
