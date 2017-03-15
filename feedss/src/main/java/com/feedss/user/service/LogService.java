package com.feedss.user.service;

import java.util.List;

import com.feedss.user.entity.Log;

/**
 * 日志服务
 * 
 * @author tangjun
 *
 */
public interface LogService {

	List<Log> add(List<Log> logs);

}
