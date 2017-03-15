package com.feedss.manage.controller;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.feedss.manage.service.ManageApplyService;
import com.feedss.manage.util.Constants;
import com.feedss.user.model.UserVo;
/**
 * 审核
 * @author 张杰
 *
 */
@Controller
@RequestMapping("/manage/apply")
public class ApplyController {

	@Autowired
	ManageApplyService applyService;
	
	@RequestMapping(value="list",method={RequestMethod.GET,RequestMethod.POST})
	public String list(String uuid,String nickname,String mobile,
			String field,String direction,@RequestParam(defaultValue="1")int page,
			@RequestParam(defaultValue="20")int size,HttpSession session,Model model){
		UserVo user=(UserVo)session.getAttribute(Constants.USER_SESSION);
		Map<String, Object> map=applyService.pageFindApply(uuid, nickname, mobile, field,
				direction, page, size, user);
		model.addAllAttributes(map);
		model.addAttribute("uuid", uuid);
		model.addAttribute("nickname", nickname);
		model.addAttribute("mobile", mobile);
		model.addAttribute("field", field);
		model.addAttribute("direction", direction);
		return "/manage/apply/list";
	}
	
	@RequestMapping(value="applyPass",method={RequestMethod.GET,RequestMethod.POST})
	@ResponseBody
	public String applyPass(String applyId,HttpSession session){
		UserVo user=(UserVo)session.getAttribute(Constants.USER_SESSION);
		JSONObject row=applyService.applyPass(user, applyId);
		return row.toJSONString();
	}
	
	@RequestMapping(value="rejected",method={RequestMethod.GET,RequestMethod.POST})
	@ResponseBody
	public String rejected(String applyId,HttpSession session){
		UserVo user=(UserVo)session.getAttribute(Constants.USER_SESSION);
		JSONObject row=applyService.rejected(user, applyId);
		return row.toJSONString();
	}
}
