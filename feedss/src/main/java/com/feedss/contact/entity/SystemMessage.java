package com.feedss.contact.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;



/**
 * Created by shenbingtao on 2016/9/10.
 */
@Data
@Entity
@Table(name = "sysMessage")
public class SystemMessage extends com.feedss.base.BaseEntity{

    public enum Status{
        INIT,//未发布
        SENDED//已发布
    }

    @Column(length = 1024)
    private String content;//消息正文

}
