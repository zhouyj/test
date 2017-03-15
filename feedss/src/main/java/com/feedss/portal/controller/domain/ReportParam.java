package com.feedss.portal.controller.domain;

import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * Created by shenbingtao on 2016/8/7.
 */
@Data
public class ReportParam {

    private String userId;

    private String toUserId;

    private String description;//举报内容

    private String tags;

    private String keyword;

    private Integer pageNo;

    private Integer pageSize;

    public static boolean checkParam(ReportParam param){
        if(param == null){
            return false;
        }
        if (StringUtils.isEmpty(param.getDescription()) || param.getDescription().trim().length() == 0) {
            return false;
        }
        if(StringUtils.isEmpty(param.getUserId()) || StringUtils.isEmpty(param.getToUserId())){
            return false;
        }

        return true;
    }

    public static boolean checkH5(ReportParam param){
        if(param == null){
            return false;
        }
        if(StringUtils.isEmpty(param.getUserId()) || StringUtils.isEmpty(param.getToUserId())){
            return false;
        }

        return true;
    }

}
