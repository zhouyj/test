package com.feedss.manage.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.feedss.portal.entity.PushStreamTerminal;
import com.feedss.portal.service.TerminalService;

/**
 * 管理设备
 * 
 * @author zhouyujuan
 *
 */
@Controller
@RequestMapping("/manage/terminal")
public class ManageTerminalController {

	@Autowired
	TerminalService terminalService;

	@RequestMapping(value = "list", method = { RequestMethod.GET, RequestMethod.POST })
	public String list(String name, String userId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, HttpSession session, Model model) {
		Pageable pageable = new PageRequest(page, size);
		Page<PushStreamTerminal> result = terminalService.get(userId, name, pageable);

		model.addAttribute("list", result.getContent());
		model.addAttribute("totalPages", result.getTotalPages());
		model.addAttribute("pageNo", page);
		model.addAttribute("pageSize", size);

		model.addAttribute("name", name);
		model.addAttribute("userId", userId);
		return "/manage/terminal/list";
	}

	@RequestMapping(value = "add", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public int save(HttpServletRequest request, HttpSession session) {
		String name = request.getParameter("name");
		String userId = request.getParameter("userId");
		String publishUrl = request.getParameter("publishUrl");
		String type = request.getParameter("type");
		PushStreamTerminal tmp = terminalService.getByUserIdAndName(userId, name);
		if(tmp!=null){//已经存在
			return -1; 
		}
		
		if(tmp==null){
			tmp = new PushStreamTerminal();
		}
		tmp.setType(type);
		tmp.setName(name);
		tmp.setPublishUrl(publishUrl);
		tmp.setUserId(userId);
		terminalService.save(tmp);

		return 0;
	}

	@RequestMapping(value = "del", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public int del(String terminalId, HttpSession session) {
		terminalService.del(terminalId);

		return 0;
	}
}
