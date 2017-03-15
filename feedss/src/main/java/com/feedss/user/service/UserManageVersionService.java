package com.feedss.user.service;

import com.feedss.user.entity.Version;

/**
 * 版本管理
 * @author 张杰
 *
 */
public interface UserManageVersionService {

	/**
	 * 某个渠道最新版本
	 * @param channel
	 * @return
	 */
	Version getNewVersion(String channel);

}
