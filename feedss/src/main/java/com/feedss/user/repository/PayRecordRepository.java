package com.feedss.user.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.PayRecord;

/**
 * Created by qinqiang on 2016/8/7.
 */
public interface PayRecordRepository extends  BaseRepository<PayRecord>{

    @Query("select u from PayRecord u where u.orderNo=:orderNo")
    public PayRecord findByOrderNo(@Param("orderNo")String orderNo);
}
