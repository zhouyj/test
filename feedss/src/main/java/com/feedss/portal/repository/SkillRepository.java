package com.feedss.portal.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.feedss.portal.entity.Skill;

import java.util.List;

/**
 * Created by shenbingtao on 2016/7/23.
 */
@Repository
public interface SkillRepository extends CrudRepository<Skill, String> {

    @Query(value = "select a.* from skill a where a.creator = ?1 and a.type= ?2 order by a.created desc limit ?3,?4", nativeQuery = true)
    public List<Skill> getContentListByPage(String userId, String contentType, int offset, int size);

    @Query("select count(*) from Skill a where a.creator = ?1 and a.type= ?2")
    public int getContentListTotalCount(String userId, String contentType);

    @Query("select count(*) from Skill a where a.creator = ?1")
    public int getContentListTotalCount(String userId);

}
