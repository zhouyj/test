package com.feedss.portal.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.feedss.content.entity.Stream;
import com.feedss.portal.entity.PushStreamTerminal;

public interface TerminalService {

	PushStreamTerminal save(PushStreamTerminal t);

	PushStreamTerminal bindUser(String terminalId, String userId);

	Page<PushStreamTerminal> get(String userId, String name, Pageable pageable);
	
	List<PushStreamTerminal> getByUserId(String userId);
	
	PushStreamTerminal getByUserIdAndName(String userId, String name);

	void del(String terminalId);
	
	PushStreamTerminal get(String terminalId);
	
	/**
	 * 根据推流地址获取使用该设备正在推流中的streamId
	 * @param publicUrl
	 * @return
	 */
	String getPublishingStream(String publishUrl);
	
	/**
	 * 根据流地址获取使用该设备推流的最新的流地址
	 * @param publishUrl
	 * @return
	 */
	Stream getLatestStream(String publishUrl);
	
	PushStreamTerminal getByStreamId(String streamId);
}
