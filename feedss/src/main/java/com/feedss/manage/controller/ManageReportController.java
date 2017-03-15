package com.feedss.manage.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feedss.portal.common.Pager;
import com.feedss.portal.controller.domain.ReportParam;
import com.feedss.portal.entity.Report;
import com.feedss.portal.service.ReportService;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.UserService;

@Controller
@RequestMapping("/manage/report/")
public class ManageReportController {

    @Autowired
    ReportService reportService;
    @Autowired
    UserService userService;

    @RequestMapping(value = "list", method = {RequestMethod.GET, RequestMethod.POST})
    public String list(@RequestParam(required = false, defaultValue = "") String userId,
                       @RequestParam(required = false, defaultValue = "") String toUserId,
                       @RequestParam(required = false, defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size, HttpSession session, Model model) {
        
        ReportParam param = new ReportParam();
        param.setPageNo(page);
        param.setPageSize(size);
        param.setUserId(userId);
        param.setToUserId(toUserId);
        param.setKeyword(keyword);

        Pager pager = new Pager(param.getPageNo(), param.getPageSize());
        List<Report> list = reportService.list(pager, param);

        JSONArray array = new JSONArray();
        if(list!=null && !list.isEmpty()){
        	array = JSONArray.parseArray(JSONObject.toJSONString(list));
        	processList(array);
        }
        
        model.addAttribute("list", array);
        model.addAttribute("pager", pager);
        model.addAttribute("userId", userId);
        model.addAttribute("toUserId", toUserId);
        model.addAttribute("keyword", keyword);


        model.addAttribute("totalCount", pager.getTotal());
        model.addAttribute("pageNo", pager.getPage());
        model.addAttribute("pageSize", pager.getPageSize());
        model.addAttribute("totalPages", pager.getTotalPage());
        return "/manage/report/list";
    }
    
    private void processList(JSONArray list){
    	for (int i = 0; i < list.size(); i++) {
            JSONObject one = list.getJSONObject(i);
            String userId = one.getString("creator");
            String toUserId = one.getString("toUserId");
            String shortDesc = one.getString("description");
            if (shortDesc.length() > 20) {
                shortDesc = shortDesc.substring(0, 20);
            }
            UserVo user = userService.getUserVoByUserId(userId);
            UserVo toUser = userService.getUserVoByUserId(toUserId);
            one.put("shortDesc", shortDesc);
            one.put("nickname", "");
            one.put("toNickname", "");
            one.put("toStatus", 0);

            if(user!=null){
                one.put("nickname", user.getProfile().getNickname());
            }
            if(toUser!=null){
                one.put("toNickname", toUser.getProfile().getNickname());
                one.put("toStatus", toUser.getStatus());
            }
        }
    }
}
