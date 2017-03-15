package com.feedss.portal.controller.domain;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class FeedbackParam {

    private String description;//举报内容
    
    private String contact;//联系方式


    public static boolean checkParam(FeedbackParam param){
        if(param == null){
            return false;
        }
        if (StringUtils.isEmpty(param.getDescription()) || param.getDescription().trim().length() == 0) {
            return false;
        }

        return true;
    }

}
