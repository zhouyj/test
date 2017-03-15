package com.feedss.user.service;

import org.springframework.transaction.annotation.Transactional;

import com.feedss.user.entity.ProductRecord;

/**
 * Created by qin.qiang on 2016/8/5 0005.
 */

public interface ProductRecordService {

    public ProductRecord createProduct(String fromId,String toId,String productId,int num,int transactionAmount);
}
