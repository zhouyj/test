package com.feedss.user.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feedss.user.entity.Log;
import com.feedss.user.repository.LogRepostitory;
import com.feedss.user.service.LogService;
/**
 * 日志
 * @author 张杰
 *
 */
@Service
public class LogServiceImpl implements LogService{

	@Autowired
	LogRepostitory logRepostitory;
	
	@Override
	public List<Log> add(List<Log> logs){
		return logRepostitory.save(logs);
	}
	
}
