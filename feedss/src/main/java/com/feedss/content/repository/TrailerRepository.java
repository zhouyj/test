package com.feedss.content.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.feedss.content.entity.Trailer;

/**
 * 预告<br>
 * @author wangjingqing
 * @date 2016-07-30
 */
public interface TrailerRepository extends JpaRepository<Trailer, String> {
	
	/**
	 * 查询预告数量<br>
	 * @return
	 */
	@Query("select count(t) from Trailer t where t.status=0 and t.playTime > ?1")
	public Integer selectTrailerCount(Date date);
	
	/**
	 * 分页查询预告列表<br>
	 * @param pageable
	 * @return
	 */
	@Query("select t from Trailer t where t.status=0 and t.playTime > ?1)")
	public Page<Trailer> selectTrailerList(Date date,Pageable pageable);
	
	@Query("select t from Trailer t where t.status=0 and t.playTime > ?1 and t.aheadTime < ?2 and t.userId = ?3")
	public Trailer selectTrailerEffective(Date date,Date end,String userId);
	
	@Query("select t from Trailer t where t.status=0 and t.playTime > ?1 and t.aheadTime < ?1 and t.isSendMsg = true")
	public List<Trailer> selectTrailerToSend(Date date);
	
	@Modifying
	@Query("update Trailer t set t.isSendMsg=false where t.uuid in ?1")
	public Integer updateTrailerToSend(List<String> uuids);
	
	public Trailer findByUuid(String uuid);
	
	@Query("select t from Trailer t where t.userId=?1 and t.status=0 and t.playTime>=?2 and t.playTime<?3")
	public List<Trailer> selectUserTrailer(String userId, Date start, Date end);
}
