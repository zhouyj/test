package com.feedss.portal.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonData;
import com.feedss.portal.common.Pager;
import com.feedss.portal.controller.domain.ReportParam;
import com.feedss.portal.entity.Report;
import com.feedss.portal.service.ReportService;

/**
 * Created by shenbingtao on 2016/8/13.
 */
@Controller
@RequestMapping("/report")
public class ReportController extends BaseController {

    @Autowired
    private ReportService reportService;

    @ResponseBody
    @RequestMapping(value = "/send", method = {RequestMethod.GET, RequestMethod.POST})
    public JsonData send(HttpServletRequest request, @RequestBody String body) {
        ReportParam param = JSON.parseObject(body, ReportParam.class);
        if (!ReportParam.checkParam(param)) {
            return JsonData.fail(ErrorCode.INVALIDPARAMETERS, "请填写举报内容");
        }
        Date now = new Date();
        Report one = new Report();
        one.setCreator(param.getUserId());
        one.setToUserId(param.getToUserId());
        one.setDescription(param.getDescription());
        one.setTags(param.getTags());
        one.setCreated(now);
        one.setUpdated(now);
        reportService.add(one);

        return JsonData.success();
    }

    @ResponseBody
    @RequestMapping(value = "/list", method = {RequestMethod.GET, RequestMethod.POST})
    public JsonData list(HttpServletRequest request, @RequestBody String body) {
        ReportParam param = JSON.parseObject(body, ReportParam.class);

        Pager pager = new Pager(param.getPageNo(), param.getPageSize());
        List<Report> list = reportService.list(pager, param);
        JsonData rlt = new JsonData(ErrorCode.SUCCESS);
        rlt.addData("list", list);
        rlt.addData("pager", pager);
        return rlt;
    }


}
