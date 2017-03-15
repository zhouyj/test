package com.feedss.user.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.feedss.user.entity.ProductRecord;
import com.feedss.user.service.ProductRecordService;

/**
 * Created by qin.qiang on 2016/8/5 0005.
 */
@Service
@Transactional(propagation= Propagation.REQUIRED)
public class ProductRecordServiceImpl implements ProductRecordService{
    @Override
    public ProductRecord createProduct(String fromId, String toId, String productId, int num, int transactionAmount) {

        return null;
    }
}
