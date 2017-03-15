package com.feedss.user.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feedss.user.entity.Module;
import com.feedss.user.entity.Module.ModuleType;
import com.feedss.user.repository.ModuleRepository;
import com.feedss.user.service.ModuleService;

/**
 * 模块
 * @author 张杰
 *
 */
@Service
public class ModuleServiceImpl implements ModuleService{

	@Autowired
	ModuleRepository moduleRepository;
	
	@Override
	public Map<String,Module> getBaseModule(){
		List<Module> list=moduleRepository.getByType(ModuleType.Base.name());
		Map<String, Module> map=new HashMap<String, Module>();
		for(Module m:list){
			map.put(m.getCode(), m);
		}
		return map;
	}
	
}
