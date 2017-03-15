package com.feedss.user.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.feedss.base.BaseEntity;

/**
 * Created by qin.qiang on 2016/7/21 0021.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductTransaction extends BaseEntity{
    @Column(length = 36, nullable = false)
    private String userId;

    @Column(length = 36, nullable = false)
    private String userProductId; //用户商品表

    private String sourceType; //变化来源类型,暂时只根据productrecord变化

    private String sourceId;  //变化来源id

    private int balance;// 正数为收入，负数为消费

    private int originValue;// 原始值

    private int currentValue;// 变更值
}
