package com.feedss.user.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.ProductRecord;

import java.util.List;

/**
 * Created by qin.qiang on 2016/8/5 0005.
 */
public interface ProductRecordRepostitory extends BaseRepository<ProductRecord>{

    @Query("select u from ProductRecord u where u.type=:type and u.status=:status")
    public List<ProductRecord> findByTypeAndStatus(@Param("type")String type, @Param("status")int status);
}
