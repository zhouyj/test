package com.feedss.portal.service;

import java.util.Map;

/**
 * 排行榜
 * @author zhouyujuan
 *
 */
public interface RankService {

	public Map<String, Object> ranking(Integer category, Integer type, String userId, Integer pageNo, Integer pageSize);

	
}
