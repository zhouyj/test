package com.feedss.user.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.feedss.base.JsonData;
import com.feedss.user.entity.Favorite;
import com.feedss.user.model.FavoriteVo;

/**
 * 收藏服务
 * @author tangjun
 *
 */
public interface UserFavoriteService {

	/**
	 * 是否收藏 (批量)
	 * @param userId 
	 * @param marks  Favorite.object+"_"+Favorite.objectId
	 * @return
	 */
	List<Map<String, Object>> IsFavorite(String userId, List<Map<String, String>> list);
	
	List<Map<String, Object>> IsFavorite(String userId, JSONArray array);


	Favorite add(String userId, String object, String objectId, String type, String extAttr);

	int del(String uuid);


	/**
	 * 是否收藏
	 * @param userId
	 * @param object
	 * @param objectId
	 * @return  true:收藏  false:未收藏
	 */
	boolean isExist(String userId, String object, String objectId, String type);

	Favorite detail(String uuid);

	Favorite detail(String userId, String object, String objectId, String type);

	int del(String userId, String object, String objectId, String type);

	List<FavoriteVo> pageUserFavorit(String userId, int size, String cursorId,
			String object,String type, String direction);

	Long userFavoritCount(String userId, String object, String type);

	JsonData getFavorites(String userId, int pageSize, String cursorId, String object, String type,
			String direction);
}
