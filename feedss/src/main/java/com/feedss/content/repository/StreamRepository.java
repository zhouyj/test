package com.feedss.content.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.feedss.content.entity.Stream;

public interface StreamRepository extends CrudRepository<Stream, String> , JpaSpecificationExecutor<Stream>{

	/**
	 * 根据分类和状态查询总数量<br>
	 * 
	 * @param category
	 * @param status
	 * @param pageable
	 * @return
	 */
	@Query("select count(s) from Stream s where s.category=?1 and s.status=?2 and s.deleted=false and s.parentId=null")
	public Integer findCountByCategoryAndStatus(String category, int status);

	@Query("select count(s) from Stream s where s.status=?1 and s.deleted=false and s.parentId=null")
	public Integer findCountByCategoryAndStatus(int status);
	
	
	@Query("select s from Stream s where s.category=?1 and s.status=?2 and (s.created < ?3 or (s.created = ?3 and s.id < ?4)) and s.deleted=false and s.parentId=null")
	public Page<Stream> findByCategoryAndStatusDesc(String category, int status,Date strated,Long id, Pageable pageable);
	
	@Query("select s from Stream s where s.category=?1 and s.status=?2 and (s.created > ?3 or (s.created = ?3 and s.id > ?4)) and s.deleted=false and s.parentId=null")
	public Page<Stream> findByCategoryAndStatusAsc(String category, int status,Date strated,Long id, Pageable pageable);

	@Query("select s from Stream s where s.status=?1 and s.deleted=false and s.parentId=null")
	public Page<Stream> findByStatus(int status, Pageable pageable);
	
	@Query("select s from Stream s where s.status=?1 and (s.playCount <?2 or (s.playCount = ?2 and s.id < ?3)) and s.deleted=false and s.parentId=null")
	public Page<Stream> findByplayCountDesc(int status,Integer playCount,Long id, Pageable pageable);
	
	@Query("select s from Stream s where s.status=?1 and (s.playCount >?2 or (s.playCount = ?2 and s.id > ?3)) and s.deleted=false and s.parentId=null")
	public Page<Stream> findByplayCountAsc(int status,Integer playCount,Long id, Pageable pageable);
	
	@Query("select count(s) from Stream s where s.userId=?1 and s.deleted=false and s.parentId=null and (s.status = 1 or (s.status = 2 and s.playbackUri!=null))")
	public Integer findCountByUserId(String userId);

	@Query("select s from Stream s where s.userId=?1 and s.deleted=false and s.parentId=null  and (s.status = 1 or (s.status = 2 and s.playbackUri!=null))")
	public Page<Stream> findByUserId(String userId, Pageable pageable);

	public Iterable<Stream> findByParentId(String parentId);

	public Iterable<Stream> findByParentIdAndStatus(String parentId, int status);

	public Stream findByUuid(String uuid);

	@Modifying
	@Query("update Stream s set s.deleted=true where s.userId=?1 and s.uuid =?2")
	public Integer deleteByUserIdAndUUId(String userId, String uuid);

	@Query("select s from Stream s where s.parentId=?1 and s.deleted=false and s.status in (0,1)")
	public List<Stream> selectRequsetList(String parentId);

	@Modifying
	@Query("update Stream s set s.status=?1,s.ended=?2 where s.uuid in ?3")
	public Integer closeStreams(Integer status,Date ended, List<String> streamIds);

	public Stream findById(Long id);

	@Query("select count(s) from Stream s where s.category=?1 and s.status=?2 and s.started >=?3 and s.deleted=false and s.parentId=null")
	public Integer selectByStartedGreaterThan(String category, Integer status, Date started, Sort sort);
	
	@Query("select count(s) from Stream s where s.status=?1 and s.started >=?2 and s.deleted=false and s.parentId=null")
	public Integer selectByStartedGreaterThan(Integer status, Date started, Sort sort);

	@Query("select count(s) from Stream s where s.status=?1 and ( s.playCount > ?2 or (s.playCount = ?2 AND s.id >?3) )and s.deleted=false and s.parentId=null")
	public Integer selectByPlayCountGreaterThan(Integer status, Integer playCount,Long id, Sort sort);
	
	@Query("select count(s) from Stream s where s.deleted=false and s.parentId=null and s.userId in ?1")
	public Integer selectByUserIds(List<String> userIds);

	@Query("select s from Stream s where s.deleted=false and s.parentId=null and s.userId in ?1")
	public Page<Stream> selectByUserIds(List<String> userIds, Pageable pageable);

	public Stream findByAppNameAndName(String appName, String name);
	
	@Modifying
	@Query("update Stream s set s.status=?1 where s.uuid =?2")
	public Integer updateStatusByUuid(Integer status,String uuid);
	
	@Modifying
	@Query("update Stream s set s.praiseCount= s.praiseCount+?1 where s.uuid =?2")
	public Integer updatePraise(Integer count,String uuid);
	
	@Modifying
	@Query("update Stream s set s.playCount= s.playCount+?1 where s.uuid =?2")
	public Integer updatePlayCount(Integer count,String uuid);
	
	@Query("select s from Stream s where s.deleted=false and s.uuid in ?1")
	public List<Stream> selectByUuids(List<String> uuids);
	
	@Query("select s from Stream s where s.userId=?1 and s.parentId=?2 and s.deleted=false and s.status in (0,1)")
	public List<Stream> selectRequsetList(String userId,String parentId);
	
	public List<Stream> findByUserIdAndStatus(String userId, Integer status,Sort sort);
	
	@Query("select s from Stream s where s.userId=?1 and s.deleted=false")
	public Page<Stream> findAllByUserId(String userId, Pageable pageable);
	
	@Query("select s from Stream s where s.status=1 and s.deleted=false and s.parentId=null")
	public List<Stream> findPublishingStream();
	
	@Modifying
	@Query("update Stream s set s.streamStatus= ?1, s.streamUpdateDate= ?2 where s.uuid =?3")
	public Integer updateStreamStatus(Integer streamStatus, Date streamUpdateDate, String streamId);
	
	@Modifying
//	@Query("update Stream s set s.playbackUri= concat(s.playbackUri,',',?1) where s.uuid =?2")
	@Query("update Stream s set s.playbackUri= ?1 where s.uuid =?2")
	public Integer updatePlayBackUri(String playBackUri,String uuid);
	
	public List<Stream> findByUserIdAndStatus(String userId,Integer status);
	
	@Query("select s from Stream s where s.userId= ?1 and s.parentId=null and s.deleted=false and s.status in (0,1)")
	public List<Stream> findByUserAndActive(String userId);
	
	@Modifying
	@Query("update Stream s SET s.category=?1 WHERE s.category=?2")
	public Integer updateStreamCategory(String categoryNew,String categoryOld);
	
	@Query("select sum(TIME_TO_SEC(timediff(s.ended, s.started))) from Stream s where s.deleted=false and s.parentId=null and s.ended is not null and s.status=2 and s.userId=?1")
	public int selectStreamTime(String userId);
	
	@Query("select count(1) from Stream s where s.deleted=false and s.parentId=null and s.ended is not null and s.status=2 and s.userId=?1")
	public int hasStream(String userId);
	
	@Query("select s from Stream s where s.status=1 and s.deleted=false and s.parentId=null and s.userId=?1 order by s.created desc")
	public List<Stream> findPublishingStream(String userId);
}
