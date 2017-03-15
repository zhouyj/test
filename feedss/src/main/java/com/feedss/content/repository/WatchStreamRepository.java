package com.feedss.content.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.feedss.content.entity.WatchStream;

/**
 * banner<br>
 * @author wangjingqing
 * @date 2016-07-30
 */
public interface WatchStreamRepository extends JpaRepository<WatchStream, String>{
	
	@Query(value="select count(1) from (select creator from watch_stream where created > ?1 and type =?2 group by creator ) a",nativeQuery=true)
	public Integer selectWatchRankingCount(Date startTime,String type);
	
//	@Query(value="select uuid,created, description,ext_attr,id, name, rank,remark,status, tags, type,updated, updater,stream_id,creator,sum(watch_time) watch_time from watch_stream where created > ?1 and type =?2 group by creator ORDER BY watch_time DESC LIMIT ?3,?4",nativeQuery=true)
//	@Query(value="select ANY_VALUE(uuid) uuid,ANY_VALUE(created) created, ANY_VALUE(description) description,ANY_VALUE(ext_attr) ext_attr,ANY_VALUE(id) id, ANY_VALUE(name) name, ANY_VALUE(rank) rank,ANY_VALUE(remark) remark,ANY_VALUE(status) status, ANY_VALUE(tags) tags, ANY_VALUE(type) type, ANY_VALUE(updated) updated, ANY_VALUE(updater) updater,ANY_VALUE(stream_id) stream_id, ANY_VALUE(sort) sort,creator,sum(watch_time) watch_time from watch_stream where created > ?1 and type =?2 group by creator ORDER BY watch_time DESC LIMIT ?3,?4",nativeQuery=true)	
	@Query(value="select *,sum(watch_time) watch_time from watch_stream where created > ?1 and type =?2 group by creator ORDER BY watch_time DESC LIMIT ?3,?4",nativeQuery=true)
	public List<WatchStream> selectWatchRanking(Date startTime,String type,Integer pageNO,Integer pageSize);
	
	public List<WatchStream> findByStatus(Integer status);
}
