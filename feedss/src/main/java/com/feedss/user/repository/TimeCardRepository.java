package com.feedss.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.TimeCard;

public interface TimeCardRepository extends JpaRepository<TimeCard, String>{

    @Query("select c from TimeCard c where c.serialNumber=:serialNumber")
    public TimeCard find(@Param("serialNumber")String serialNumber);
    
    @Query("select c from TimeCard c where c.userId=:userId")
    public TimeCard findByUser(@Param("userId")String userId);

}
