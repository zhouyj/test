package com.feedss.user.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.AccountTransaction;

/**
 * Created by qin.qiang on 2016/8/5 0005.
 */
public interface AccountTransactionRepository extends BaseRepository<AccountTransaction>{
	
	@Query("select SUM(at.balance) from AccountTransaction at where at.userId=:userId and (at.expired is null or at.expired>now())")
	public Integer getBalance(@Param("userId")String userId);
	
	@Query("select count(1) from AccountTransaction at where at.userId=:userId and sourceType=:sourceType and created>=:start and created<=:end")
	public int getCount(@Param("userId") String userId, @Param("sourceType") String sourceType, @Param("start") Date start, @Param("end") Date end);
}
