package com.feedss.contact.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.contact.common.Pager;
import com.feedss.contact.controller.model.SysMessageParam;
import com.feedss.contact.entity.SystemMessage;
import com.feedss.contact.service.MessageService;

@Controller
@RequestMapping("/sysmessage")
public class SystemMessageController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MessageService messageService;

    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public ResponseEntity<Object> list(HttpServletRequest request, @RequestBody String msgStr) {
        logger.info("request body, " + msgStr);
        SysMessageParam param = JSON.parseObject(msgStr, SysMessageParam.class);
        if(param.getPage()<1) param.setPage(1);
        try {
            Pager pager = new Pager(param.getPage(), param.getPageSize());
            List<SystemMessage> list = messageService.getSystemMessage(param.getPage(), param.getPageSize(), param.getKeyword());
            JSONObject data = new JSONObject();
            data.put("list", list);
            data.put("pager", pager);

            return JsonResponse.success(data);
        } catch (Exception e) {
            logger.error("list error", e);
        }

        return JsonResponse.fail(ErrorCode.INTERNALERROR);
    }

    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ResponseEntity<Object> add(HttpServletRequest request, @RequestBody String msgStr) {
        logger.info("request body, " + msgStr);
        SysMessageParam param = JSON.parseObject(msgStr, SysMessageParam.class);
        try {
            if (null != messageService.createSystemMessage(param.getContent())) {
                return JsonResponse.success();
            }
        } catch (Exception e) {
            logger.error("sendSystemMessage error", e);
        }

        return JsonResponse.fail(ErrorCode.INTERNALERROR);
    }

    @ResponseBody
    @RequestMapping(value = "/modify", method = RequestMethod.POST)
    public ResponseEntity<Object> modify(HttpServletRequest request, @RequestBody String msgStr) {
        logger.info("request body, " + msgStr);
        SysMessageParam param = JSON.parseObject(msgStr, SysMessageParam.class);
        try {
        	if(messageService.modifySystemMessage(param.getUuid(), param.getContent())){
        		return JsonResponse.success();
        	}
        } catch (Exception e) {
            logger.error("sendSystemMessage error", e);
        }

        return JsonResponse.fail(ErrorCode.INTERNALERROR);
    }

    @ResponseBody
    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public ResponseEntity<Object> send(HttpServletRequest request, @RequestBody String msgStr) {
        logger.info("request body, " + msgStr);
        SysMessageParam param = JSON.parseObject(msgStr, SysMessageParam.class);
        if(messageService.sendSystemMessage(param.getUuid())){
        	return JsonResponse.success();
        }

        return JsonResponse.fail(ErrorCode.INTERNALERROR);
    }

}
