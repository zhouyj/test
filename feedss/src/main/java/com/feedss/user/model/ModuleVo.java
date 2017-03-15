package com.feedss.user.model;

import java.util.HashMap;
import java.util.List;

import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.feedss.user.entity.Module;

/**
 * 模块vo
 * @author 张杰
 *
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ModuleVo {

	private String url;
	private String code;
	private String ioc;
	private String num;
	private String name;
	private String executeType;
	
	
	public ModuleVo(Module module){
		this.url=module.getUrl();
		this.name=module.getName();
		this.code=module.getCode();
		this.ioc=module.getIoc();
		this.executeType=module.getExecuteType();
	}
}
