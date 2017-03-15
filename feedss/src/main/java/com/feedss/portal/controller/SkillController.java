package com.feedss.portal.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonData;
import com.feedss.portal.common.ErrorConstants;
import com.feedss.portal.common.Pager;
import com.feedss.portal.controller.domain.SkillParam;
import com.feedss.portal.entity.Skill;
import com.feedss.portal.service.SkillService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/skill")
public class SkillController extends BaseController {

    @Resource
    private SkillService skillService;

    //h5页面
    @Deprecated
    @RequestMapping(value = "/contentList", method = RequestMethod.POST)
    public String skillList(HttpServletRequest request, @RequestBody String msgStr) {
        SkillParam param = JSON.parseObject(msgStr, SkillParam.class);
        Pager pager = new Pager(param.getPageNo(), param.getPagSize());
        List<Skill> list = skillService.getContentList(param.getUserId(), param.getType(), pager);

        request.setAttribute("pager", pager);
        request.setAttribute("list", list);
        request.setAttribute("type", param.getType());
        request.setAttribute("userId", param.getUserId());
        return "/skill/list";
    }

    @ResponseBody
    @RequestMapping(value = "/publishContent", method = RequestMethod.POST)
    public JsonData publish(HttpServletRequest request, @RequestBody String msgStr) {
    	log.info("publish content..." + msgStr);
        SkillParam param = JSON.parseObject(msgStr, SkillParam.class);
        
        if(StringUtils.isNotBlank(param.getDescription()) && param.getDescription().length()>200){
        	return JsonData.fail(ErrorCode.INVALID_PARAMETERS, "描述文字不能超过200字");
        }
        
        if (param.getImgUrl().length() < 5) {
            return JsonData.fail(ErrorCode.INVALID_PARAMETERS, "图片地址不正确");
        }
        try{
        	Skill one = skillService.addContent(param.getUserId(), param.getType(), param.getDescription(), param.getImgUrl());
            if (one == null) {
                return JsonData.fail(ErrorCode.INTERNAL_ERROR, ErrorConstants.COMMON_ERROR_MSG);
            }
        }catch (Exception e) {
			log.error("publish content error", e);
			return JsonData.fail(ErrorCode.INTERNAL_ERROR, ErrorConstants.COMMON_ERROR_MSG);
		}
        
        return JsonData.success();
    }
    
    @ResponseBody
    @RequestMapping(value = "/delContent", method = RequestMethod.POST)
    public JsonData delContent(HttpServletRequest request, @RequestBody String msgStr) {
        SkillParam param = JSON.parseObject(msgStr, SkillParam.class);
        Skill one = skillService.getContent(param.getContentId());
        if(one == null){
            return JsonData.fail(ErrorCode.INVALID_PARAMETERS, "内容不存在");
        }
        if(!one.getCreator().equals(getUserIdFromHeader(request))){
            return JsonData.fail(ErrorCode.NO_AUTH, "不是内容发布者本人");
        }
        boolean flag = skillService.delContent(param.getContentId());
        if (!flag) {
            return JsonData.fail(ErrorCode.BALANCE_NOT_ENOUGH, "删除失败");
        }

        return JsonData.success();
    }

}
