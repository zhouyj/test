package com.feedss.user.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.Version;

/**
 * 版本管理
 * @author 张杰
 *
 */
public interface VersionRepository extends BaseRepository<Version>,JpaSpecificationExecutor<Version> {

	@Query("select mv from ManageVersion mv where mv.version=:version and mv.channel=:channel")
	public Page<Version> gets(@Param("version")String version,
			@Param("channel")String channel,Pageable page); 
	
	@Query("select mv.channel from ManageVersion mv  group by mv.channel")
	public List<String> getChannels();
	
	@Query("select mv from ManageVersion mv where mv.channel=:channel "
			+ "order by mv.version_code desc ")
	public Page<Version> getNewVersion(@Param("channel")String channel, Pageable pageable);
	
	@Modifying
	@Query("update ManageVersion mv set mv.status=:status where mv.uuid=:uuid")
	public int updateStatus(@Param("uuid")String uuid,@Param("status")int status);
	
}
