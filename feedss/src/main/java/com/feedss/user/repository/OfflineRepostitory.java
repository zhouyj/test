package com.feedss.user.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.Offline;
/**
 * 离线
 * @author zhangjie
 *
 */
public interface OfflineRepostitory extends BaseRepository<Offline> {

	
	@Query("select sum(o.integral) from Offline as o where o.userId=:userId and o.action=:action")
	public Long getIntegral(@Param("userId")String userId,@Param("action")String action);
	
}
