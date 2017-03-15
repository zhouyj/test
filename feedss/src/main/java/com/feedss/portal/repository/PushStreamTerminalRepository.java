package com.feedss.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.feedss.portal.entity.PushStreamTerminal;

public interface PushStreamTerminalRepository extends CrudRepository<PushStreamTerminal, String> , JpaSpecificationExecutor<PushStreamTerminal>{

	@Query("select p from PushStreamTerminal p where p.status=?1 order by created")
	public List<PushStreamTerminal> findByStatus(int status);
	
	@Query("select p from PushStreamTerminal p where p.userId=?1 and p.status=?2 order by created")
	public List<PushStreamTerminal> findByUserIdAndStatus(String userId, int status);
	
	@Query("select p from PushStreamTerminal p where p.userId=?1 and p.name=?2")
	public PushStreamTerminal findByUserIdAndName(String userId, String name);
	
	public PushStreamTerminal findByStreamId(String streamId);
	
	@Query("select p from PushStreamTerminal p where p.publishUrl=?1 and p.status=?2 and p.streamId is not null")
	public List<PushStreamTerminal> findByPublishUrl(String publishUrl, int status);
}
