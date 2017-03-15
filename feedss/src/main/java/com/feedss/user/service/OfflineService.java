package com.feedss.user.service;

import com.feedss.user.entity.Offline;

/**
 * 离线
 * @author 张杰
 *
 */
public interface OfflineService {

	Offline add(String userId, String action, String object, String objectId,
			long duration, String ext);

}
