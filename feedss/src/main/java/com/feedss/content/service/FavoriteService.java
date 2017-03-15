package com.feedss.content.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.feedss.content.entity.Product;
import com.feedss.content.entity.ReleaseProduct.ReleaseType;
import com.feedss.content.model.FavoriteItem;
import com.feedss.content.repository.ProductRepository;
import com.feedss.user.entity.Favorite.FavoriteType;
import com.feedss.user.model.FavoriteVo;
import com.feedss.user.service.UserFavoriteService;

/**
 * 收藏<br>
 * @author wangjingqing
 * @date 2016-08-18
 */
@Component
public class FavoriteService {

	//日志
	Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private UserFavoriteService userFavoriteService;
	
	/**
	 * 是否已收藏
	 * @param paramList
	 * @param userId
	 * @return
	 */
	public Map<String, Integer> isFavorited(List<String> productIds, String userId){
		Map<String, Integer> result = new HashMap<String, Integer>();
		List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
		for(String productId:productIds){
			Map<String, String> param = new HashMap<String, String>();
			param.put("objectId", productId);
			param.put("object", ReleaseType.Product.name());
			paramList.add(param);
		}
		List<Map<String, Object>> favoritList = userFavoriteService.IsFavorite(userId, paramList);
		if(favoritList!=null && !favoritList.isEmpty()){
			for(Map<String, Object> map:favoritList){
				String objectStr = (String) map.get("object");
				String objectIdStr = (String) map.get("objectId");
				if(ReleaseType.Product.name().equals(objectStr)){
					int isFavorite = (int) map.get("isFavorite");
					result.put(objectIdStr, isFavorite);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * 添加收藏<br>
	 */
	public boolean addFavorite(String userId,String productId){
		userFavoriteService.add(userId, ReleaseType.Product.name(), productId, FavoriteType.FAVORITE.name(), null);
		return true;
	}
	
	/**
	 * 删除收藏<br>
	 * @param userId
	 * @param productId
	 * @return
	 */
	public boolean deleteFavorite(String userId,String productId){
		Map<String, String> params = new HashMap<String, String>();
		params.put("objectId", productId);
		params.put("object", ReleaseType.Product.name());
		userFavoriteService.del(userId, ReleaseType.Product.name(), productId, FavoriteType.FAVORITE.name());
		return true;
	}
	
	/**
	 * 获取收藏列表
	 * @param userId
	 * @return
	 */
	public List<FavoriteItem<Product>> getFavorites(String userId, String cursorId, int pageSize){
		List<FavoriteItem<Product>> favorites = new ArrayList<FavoriteItem<Product>>();
		List<FavoriteVo> list=userFavoriteService.pageUserFavorit(userId, pageSize, cursorId, ReleaseType.Product.name(), FavoriteType.FAVORITE.name(), null);
		for(FavoriteVo f:list){
			if(ReleaseType.Product.name().equals(f.getObject())){
				FavoriteItem<Product> item = new FavoriteItem<Product>();
				item.setCreated(f.getCreated().getTime());
				item.setObject(f.getObject());
				item.setObjectId(f.getObjectId());
				item.setUuid(f.getUuid());
				Product p = productRepository.findOne(f.getObjectId());
				if(p==null) continue;
				p.setProductId(p.getUuid());//页面显示uuid不是真实的productId
				item.setT(p);
				favorites.add(item);
			}
		}
		return favorites;
	}
}
