package com.feedss.user.service;

import java.util.HashMap;
import java.util.List;

/**
 * Created by qin.qiang on 2016/8/4 0004.
 */
public interface UserProductService {
	
	public List<HashMap<String, Object>> getProductsByUserId(String userId);

	public long getProductsSumByUserId(String userId);
}
