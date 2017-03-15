package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.feedss.base.BaseEntity;

import lombok.Data;
/**
 * 安卓版本
 * @author 张杰
 *
 */
@Table(name="t_manage_version")
@Entity
@Data
public class Version extends BaseEntity{

	private String version;//版本
	
	private int version_code;//版本号
	
	private String device_type;//设备类型
	
	@Column(columnDefinition="text")
	private String update_info;//升级内容
	
	private int update_type;//更新类型 0:普通 1:强升
	
	private String down_url;//下载地址
	
	private String channel;//渠道
	
}
