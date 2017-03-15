package com.feedss.portal.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.feedss.base.BaseEntity;

@Data
@EqualsAndHashCode(callSuper=true)
@Entity
@Table(name = "skill")
public class Skill extends BaseEntity {

    public enum SkillType {
        PRODUCT,   //产品
        SKILL,   //技能
        WORKS //作品
    }

    //用到的字段
    //creator  作者
    //decription 内容
    //type 类型

    @Column(length = 256, nullable = false)
    private String imgUrl;// 图片URL

}
