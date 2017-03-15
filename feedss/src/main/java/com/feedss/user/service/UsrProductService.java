package com.feedss.user.service;

import java.util.List;

import com.feedss.base.ErrorCode;
import com.feedss.base.Pages;
import com.feedss.user.entity.UsrProduct;
import com.feedss.user.model.ProductVo;

/**
 * Created by qin.qiang on 2016/8/3 0003.
 */
public interface UsrProductService {

	public List<ProductVo> getProductsByType(String type);

	public ErrorCode giveGift(String fromUserId, String toUserId, String productId, int num);

	public UsrProduct getProductById(String productId);

	public void receiveGift();

	/**
	 * 分页查询商品<br>
	 * 
	 * @param userId
	 * @param pageNo
	 * @param pageSize
	 * @param status
	 */
	public Pages<UsrProduct> selectProductList(String category, Integer pageNo, Integer pageSize, Integer status);
	
	/**
	 * 分页查询商品<br>
	 * 
	 * @param categoryList
	 * @param pageNo
	 * @param pageSize
	 * @param status
	 */
	public Pages<UsrProduct> selectProductListByCategoryList(List<String> categoryList, Integer pageNo, Integer pageSize, Integer status);
	
    public boolean deleteUsrProducts(String[] ids);
}
