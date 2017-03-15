package com.feedss.portal.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.feedss.portal.entity.Report;

import java.util.List;

/**
 * Created by shenbingtao on 2016/7/23.
 */
@Repository
public interface ReportRepository extends CrudRepository<Report, String> {

    @Query(value = "select a.* from report a where 1=1 order by a.created desc limit ?1,?2", nativeQuery = true)
    public List<Report> getListByPage(int offset, int size);

    @Query(value = "select a.* from report a where 1=1 and creator=?3 order by a.created desc limit ?1,?2", nativeQuery = true)
    public List<Report> getListByPageUserId(int offset, int size, String userId);

    @Query(value = "select a.* from report a where 1=1 and to_user_id=?3 order by a.created desc limit ?1,?2", nativeQuery = true)
    public List<Report> getListByPageToUserId(int offset, int size, String toUserId);

    @Query(value = "select a.* from report a where 1=1 and description like ?3 order by a.created desc limit ?1,?2", nativeQuery = true)
    public List<Report> getListByPageKeyword(int offset, int size, String keyword);

    @Query("select count(*) from Report a where 1=1")
    public int getTotalCount();

    @Query("select count(*) from Report a where 1=1 and creator=?1")
    public int getTotalCountUserId(String userId);

    @Query("select count(*) from Report a where 1=1 and to_user_id=?1")
    public int getTotalCountToUserId(String toUserId);

    @Query("select count(*) from Report a where 1=1 and description like ?1")
    public int getTotalCountKeyword(String keyword);

}
