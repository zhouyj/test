package com.feedss.contact.controller.model;

import lombok.Data;

/**
 * Created by shenbingtao on 2016/9/10.
 */
@Data
public class SysMessageParam {

    private String uuid;
    private String content;
    private int status;
    private String keyword;
    private int page;
    private int pageSize;

}
