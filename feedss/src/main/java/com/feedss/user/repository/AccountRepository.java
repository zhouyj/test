package com.feedss.user.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.Account;

/**
 * Created by qin.qiang on 2016/8/2 0002.
 */
public interface AccountRepository extends BaseRepository<Account>{

    @Query("select u from Account u where u.userId=:userId")
    public Account findByUserId(@Param("userId")String userId);

}
