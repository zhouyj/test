package com.feedss.manage.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.feedss.base.ErrorCode;
import com.feedss.base.JsonData;
import com.feedss.contact.entity.SystemMessage;
import com.feedss.contact.service.MessageService;

@Controller
@RequestMapping("/manage/sysmessage/")
public class ManageSysMessageController {

    public String mediaType = "application/json";

    public String charset = "utf-8";

    @Autowired
    private MessageService messageService;

    @RequestMapping(value = "list", method = {RequestMethod.GET})
    public String list(
                       @RequestParam(required = false, defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size, HttpSession session, Model model) {
        List<SystemMessage> list = messageService.getSystemMessage(page, size, keyword);
        model.addAttribute("list", list);
        model.addAttribute("pageNo", page);
        model.addAttribute("pageSize", size);
//      model.addAttribute("totalCount", pager.getInteger("total"));
//      model.addAttribute("totalPages", pager.getInteger("totalPage"));
        return "/manage/sysmessage/list";
    }

    @ResponseBody
    @RequestMapping(value = "modify", method = {RequestMethod.POST})
    public JsonData modify(@RequestParam String uuid,
                       @RequestParam String content,
                       HttpSession session, Model model) {
        if (messageService.modifySystemMessage(uuid, content)) {
            return JsonData.success();
        }
        return JsonData.fail(ErrorCode.FAIL);
    }

    @ResponseBody
    @RequestMapping(value = "add", method = {RequestMethod.POST})
    public JsonData add(@RequestParam String content,
                         HttpSession session, Model model) {
    	if(messageService.createSystemMessage(content)!=null){
    		 return JsonData.success();
        }
        return JsonData.fail(ErrorCode.FAIL);
    }

    @ResponseBody
    @RequestMapping(value = "send", method = {RequestMethod.POST})
    public JsonData send(@RequestParam String uuid,
                         HttpSession session, Model model) {
    	if(messageService.sendSystemMessage(uuid)){
    		 return JsonData.success();
        }
        return JsonData.fail(ErrorCode.FAIL);
    }


}
