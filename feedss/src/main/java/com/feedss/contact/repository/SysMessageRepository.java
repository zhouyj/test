package com.feedss.contact.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.feedss.contact.entity.SystemMessage;

public interface SysMessageRepository extends CrudRepository<SystemMessage, String> {


    @Query(value = "select a.* from sys_message a where 1=1 order by a.created desc limit ?1,?2", nativeQuery = true)
    public List<SystemMessage> getListByPage(int offset, int size);


    @Query(value = "select count(*) from sys_message a where 1=1", nativeQuery = true)
    public int getTotalCount();

    @Query(value = "select a.* from sys_message a where 1=1 and content like ?3 order by a.created desc limit ?1,?2", nativeQuery = true)
    public List<SystemMessage> getListByPageKeyWord(int offset, int size, String keyword);


    @Query(value = "select count(*) from sys_message a where 1=1 and content like ?1", nativeQuery = true)
    public int getTotalCountKeyWord(String keyword);


}
