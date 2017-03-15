package com.feedss.manage.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.Pages;
import com.feedss.content.entity.Trailer;
import com.feedss.content.entity.Trailer.TrailerStatus;
import com.feedss.content.service.TrailerService;
/**
 * 预告<br>
 * @author wangjingqing
 * @date 2016-08-20
 */
@Controller
@RequestMapping("/manage/trailer")
public class ManageTrailerController {
	
	@Autowired
	private TrailerService trailerService;
	/**
	 * 预告列表<br>
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/",method=RequestMethod.GET)
	public String list(HttpServletRequest request, HttpSession session, Model model){
		Integer status = request.getParameter("status") == null?0:Integer.valueOf(request.getParameter("status"));
		Integer pageNo = request.getParameter("pageNo") == null?1: Integer.valueOf(request.getParameter("pageNo"));
		Integer pageSize = request.getParameter("pageSize") == null ?10:Integer.valueOf(request.getParameter("pageSize"));
		String trailerId = request.getParameter("trailerId");
		String title = request.getParameter("title");
		String userId = request.getParameter("userId");
//		Pages<TrailerVo> pages = manageTrailerService.selectTrailerList(status, trailerId, title, userId, pageNo, pageSize, userVo);
		Integer count = trailerService.selectTrailerListCount(status, trailerId, title, userId, pageNo, pageSize);
		List<Trailer> list = new ArrayList<Trailer>();
		if(count.intValue() > 0){
			list = trailerService.selectTrailerList(status, trailerId, title, userId, pageNo, pageSize);
		}
		Pages<Trailer> pages = new Pages<Trailer>(count, list);
		model.addAttribute("status", status);
		model.addAttribute("pageNo", pageNo);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("trailerId", trailerId);
		model.addAttribute("title", title);
		model.addAttribute("userId", userId);
		model.addAttribute("totalCount", pages.getTotalCount());
		
		model.addAttribute("list", list);
		model.addAttribute("pageCount", pages.getPageCount());
		return "/manage/trailer/list";
	}
	
	/**
	 * 获取预告<br>
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/get",method=RequestMethod.GET)
	public String get(HttpServletRequest request, HttpSession session, Model model){
		String trailerId = request.getParameter("trailerId");
		Trailer trailer=null;
		if(trailerId != null){
			trailer = trailerService.findByUuid(trailerId);
		}
		if(trailer == null){
			trailer = new Trailer();
		}
		model.addAttribute("trailer", trailer);
		return "/manage/trailer/detail";
	}
	/**
	 * 添加预告页面<br>
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/add",method=RequestMethod.GET)
	public String addPage(HttpServletRequest request, HttpSession session, Model model){
		String trailerId = request.getParameter("trailerId");
		Trailer trailer=null;
		if(trailerId != null){
			trailer = trailerService.findByUuid(trailerId);
		}
		if(trailer == null){
			trailer = new Trailer();
			trailer.setUuid(null);
		}
		model.addAttribute("trailer", trailer);
		return "/manage/trailer/add";
	}
	
	/**
	 * 添加或编辑预告<br>
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/modify",method=RequestMethod.POST)
	public ResponseEntity<Object> modify(HttpServletRequest request,HttpSession session, Model model){
		String trailerId = request.getParameter("trailerId");
		String title = request.getParameter("title");
		if(StringUtils.isBlank(title)){
			return JsonResponse.fail(ErrorCode.TITLE_IS_EMPTY);
		}
		String userId = request.getParameter("userId");
		if(StringUtils.isBlank(userId)){
			return JsonResponse.fail(ErrorCode.CHOOSE_USER);
		}
		String picUrl = request.getParameter("picUrl");
		if(StringUtils.isBlank(picUrl)){
			return JsonResponse.fail(ErrorCode.UPLOAD_IMAGE);
		}
		String content = request.getParameter("content");
		if(StringUtils.isNotBlank(content) && content.length()>40){
			return JsonResponse.fail(ErrorCode.DESCRIPTION_TOO_LONG);
		}
		String playTime = request.getParameter("playTime");
		if(StringUtils.isBlank(playTime)){
			return JsonResponse.fail(ErrorCode.CHOOSE_TIME);
		}
		Trailer trailer = trailerService.add(trailerId, playTime,title,
				picUrl, content, userId);
		
		if(null == trailer){
			return JsonResponse.fail(ErrorCode.INTERNAL_ERROR);
		}
		return JsonResponse.success("");
	}
	
	/**
	 * 删除预告<br>
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/delete",method=RequestMethod.GET)
	public ResponseEntity<Object> delete(HttpServletRequest request,HttpSession session, Model model){
		String trailerId = request.getParameter("trailerId");
		if(StringUtils.isBlank(trailerId)){
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}
		Trailer trailer = trailerService.findByUuid(trailerId);
		if (trailer == null) {
			return JsonResponse.fail(ErrorCode.INTERNAL_ERROR);
		}
		trailer.setStatus(TrailerStatus.Unavailable.ordinal());// 不可用
		trailerService.save(trailer);
		return JsonResponse.success("");
	}
}
