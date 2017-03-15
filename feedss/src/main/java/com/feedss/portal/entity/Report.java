package com.feedss.portal.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.feedss.base.BaseEntity;

/**
 * 举报
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Entity
@Table(name = "report")
public class Report extends BaseEntity {

    //用到的字段
    //creator  作者
    //decription 内容
    //tags 标签
    @Column(length = 36, nullable = true)
    private String toUserId;// 创建者userId

}
