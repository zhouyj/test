package com.feedss.portal.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.feedss.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 意见反馈
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Entity
@Table(name = "feedback")
public class Feedback extends BaseEntity {

}
