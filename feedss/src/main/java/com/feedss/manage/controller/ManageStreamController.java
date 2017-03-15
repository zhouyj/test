package com.feedss.manage.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.Pages;
import com.feedss.content.entity.Stream;
import com.feedss.content.entity.Stream.StreamType;
import com.feedss.content.service.CategoryService;
import com.feedss.content.service.RoomService;
import com.feedss.content.service.StreamService;
import com.feedss.manage.util.Constants;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.ProfileService;

/**
 * stream<br>
 * 
 * @author zhouyujuan
 * @date 2016-09-22
 */
@Controller
@RequestMapping("/manage/stream")
public class ManageStreamController {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private StreamService streamService;
	@Autowired
	private RoomService roomService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private ProfileService profileService;

	/**
	 * 查询stream列表<br>
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String list(HttpServletRequest request, HttpSession session, Model model) {

		Integer status = request.getParameter("status") == null ? 0 : Integer.valueOf(request.getParameter("status"));
		Integer pageNo = request.getParameter("pageNo") == null ? 1 : Integer.valueOf(request.getParameter("pageNo"));
		Integer pageSize = request.getParameter("pageSize") == null ? 10
				: Integer.valueOf(request.getParameter("pageSize"));
		String name = request.getParameter("name");
		String userId = request.getParameter("userId");

		Pages<Stream> pages = streamService.findAllStreams(status.intValue(), userId, name, pageNo.intValue(),
				pageSize.intValue());

		model.addAttribute("status", status);
		model.addAttribute("name", name);
		model.addAttribute("userId", userId);

		List<Stream> list = pages.getList();
		for (Stream stream : list) {
			stream.setUserNickname(profileService.findByUserId(stream.getUserId()).getNickname());
			if (stream.getCategory() != null) {
				stream.setCategoryName(categoryService.selectCategory(stream.getCategory()).getName());
			}
		}
		model.addAttribute("streams", list);
		model.addAttribute("pageCount", pages.getPageCount());

		long totalPages = pages.getTotalCount() % pageSize == 0 ? pages.getTotalCount() / pageSize
				: (pages.getTotalCount() / pageSize) + 1;

		model.addAttribute("pageSize", pageSize);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("totalCount", pages.getTotalCount());
		model.addAttribute("status", status);
		model.addAttribute("pageNo", pageNo);
		model.addAttribute("name", name);
		model.addAttribute("userId", userId);
		return "/manage/stream/list";
	}

	@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String add(HttpServletRequest request, HttpSession session, Model model) {
		UserVo user = (UserVo) session.getAttribute(Constants.USER_SESSION);
		model.addAttribute("userId", user.getUuid());
		model.addAttribute("categories", categoryService.selectCategoryList(0));
		return "/manage/stream/add";
	}

	@ResponseBody
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Object post(HttpServletRequest request, HttpSession session, Model model) {
		String userId = request.getParameter("userId");
		String title = request.getParameter("title");
		String categoryId = request.getParameter("categoryId");
		String cover = request.getParameter("cover");
		String secret = request.getParameter("secret");

		Double longtitude = 0D;
		if (StringUtils.isNotEmpty(request.getParameter("longtitude"))) {
			longtitude = Double.parseDouble(request.getParameter("longtitude"));
		}
		Double latitude = 0D;
		if (StringUtils.isNotEmpty(request.getParameter("latitude"))) {
			latitude = Double.parseDouble(request.getParameter("latitude"));
		}
		String position = request.getParameter("position");
		// JSONObject object = manageStreamService.createStream(userId, title,
		// categoryId, cover, secret, longtitude, latitude, position, user);
		if (roomService == null) {
			logger.error("roomService is null.....");
		}
		Stream stream = roomService.createStream(categoryId, userId, title, cover, secret, longtitude, latitude,
				position, "", "", "", "", StreamType.Normal);
		if (stream != null) {
			Map<String, Object> result = streamService.streamToMap(stream);
			return JsonResponse.success(result);
		} else {
			return JsonResponse.fail(ErrorCode.INTERNAL_FAILURE);
		}

	}

	@RequestMapping(value = "/publish", method = RequestMethod.GET)
	public String publish(HttpServletRequest request, HttpSession session, Model model) {
		model.addAttribute("publishUri", request.getParameter("publishUri"));
		return "/manage/stream/publish";
	}

	@RequestMapping(value = "/play", method = RequestMethod.GET)
	public String play(HttpServletRequest request, HttpSession session, Model model) {
		String playUri = request.getParameter("playUri");
		model.addAttribute("playUri", playUri);
		return "/manage/stream/play";
	}

	@ResponseBody
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public Object delete(HttpServletRequest request, HttpSession session, Model model) {
		String streamId = request.getParameter("streamId");
		// UserVo user = (UserVo)session.getAttribute(Constants.USER_SESSION);
		Stream stream = streamService.findStreamById(streamId);
		Integer result = streamService.deleteStream(stream.getUserId(), streamId);
		return JsonResponse.success(result);
	}

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public String test(HttpServletRequest request, HttpSession session, Model model) {
		return "/manage/stream/test";
	}

}
