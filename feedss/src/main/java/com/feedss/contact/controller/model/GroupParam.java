package com.feedss.contact.controller.model;

import lombok.Data;

/**
 * Created by shenbingtao on 2016/8/4.
 */
@Data
public class GroupParam {

    private String groupId;

    private String type; // GroupType

    private String name;

    private String ownerAccount;

    private String memberAccount;

    private int pageNo = 1;

    private int pageSize = 20;

    private String accountId;

    private String photo;

    private String nickname;

    private String cursorId;

}
