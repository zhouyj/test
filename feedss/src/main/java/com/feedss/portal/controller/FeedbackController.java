package com.feedss.portal.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonData;
import com.feedss.portal.controller.domain.FeedbackParam;
import com.feedss.portal.entity.Feedback;
import com.feedss.portal.service.ReportService;

@Controller
@RequestMapping("/feedback")
public class FeedbackController extends com.feedss.portal.controller.BaseController {

    @Autowired
    private ReportService reportService;

    @ResponseBody
    @RequestMapping(value = "/add", method = {RequestMethod.POST})
    public JsonData send(HttpServletRequest request, @RequestBody String body, @RequestHeader("userId") String userId) {
        FeedbackParam param = JSON.parseObject(body, FeedbackParam.class);
        if (!FeedbackParam.checkParam(param)) {
            return JsonData.fail(ErrorCode.INVALIDPARAMETERS, "请填写反馈内容");
        }
        Date now = new Date();
        Feedback one = new Feedback();
        one.setCreator(userId);
        one.setDescription(param.getDescription());
        one.setRemark(param.getContact());
        one.setCreated(now);
        one.setUpdated(now);
        reportService.add(one);

        return JsonData.success();
    }
}
