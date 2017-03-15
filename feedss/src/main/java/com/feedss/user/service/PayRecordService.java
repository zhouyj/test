package com.feedss.user.service;

import java.util.HashMap;

import com.feedss.user.entity.PayRecord;

/**
 * Created by qinqiang on 2016/8/7.
 */
public interface PayRecordService {

	public PayRecord saveOrUpdate(PayRecord payRecord);

	public PayRecord findByOrderNo(String orderNo);

	public HashMap<String,Object> getPayRecordList(int pageNo,int pageSize,String orderNo,String userId,String mobile,String sortDirection);

}
