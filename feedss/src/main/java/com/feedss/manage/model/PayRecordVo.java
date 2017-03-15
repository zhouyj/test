package com.feedss.manage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by qinqiang on 2016/8/27.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayRecordVo {
    private String orderNo;

    private String outOrderNo;

    private String userId;

    private String payMethod;

    private int currencyAmount;

    private int  moneyAmount;

    private Date created;

    private String nickname;

    private String mobile;
}