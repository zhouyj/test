package com.feedss.manage.controller;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.feedss.user.service.PayRecordService;

/**
 * Created by qin.qiang on 2016/8/29 0029.
 */
@Controller
@RequestMapping("/manage/payrecord")
public class ManagePayRecordController {

    @Autowired
    private PayRecordService payRecordService;

    @RequestMapping(value="list",method={RequestMethod.GET,RequestMethod.POST})
    public String list( String direction, @RequestParam(defaultValue="1")int page,
                       @RequestParam(defaultValue="20")int size,String orderNo,String userId,String mobile,
                        HttpSession session, Model model){
        Map<String, Object> map = payRecordService.getPayRecordList(page, size, orderNo, userId, mobile, direction);
        model.addAllAttributes(map);
        return "/manage/payrecord/list";
    }
}
