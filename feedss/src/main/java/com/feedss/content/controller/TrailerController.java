package com.feedss.content.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.Pages;
import com.feedss.base.util.ConvertUtil;
import com.feedss.base.util.DateUtil;
import com.feedss.content.entity.Trailer;
import com.feedss.content.entity.Trailer.TrailerStatus;
import com.feedss.content.entity.UserTrailer;
import com.feedss.content.service.TrailerService;
import com.feedss.user.model.UserProfileVo;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.UserService;

/**
 * 视频API
 * 
 * @author wangjingqing
 * @since 1.0.0
 * @date 2016-07-22
 */
@RestController
@RequestMapping("/trailer")
public class TrailerController {

	/**
	 * 预告服务
	 */
	@Autowired
	private TrailerService trailerService;

	@Autowired
	private UserService userService;
//	private UserProxyService userService;

	/**
	 * 预告API<br>
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public ResponseEntity<Object> trailer(HttpServletRequest request, @RequestBody String bodyStr,
			@RequestHeader("userId") String userId) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		Integer pageNo = body.getInteger("pageNo");// 当前页
		pageNo = (pageNo == null || pageNo.intValue() == 0) ? 0 : --pageNo;
		Integer pageSize = body.getInteger("pageSize");// 每页显示数量
		pageSize = pageSize == null ? 10 : pageSize;
		Pages<Trailer> list = trailerService.trailerList(pageNo, pageSize);
		Integer count = list.getTotalCount();
		List<Map<String, Object>> re = new ArrayList<Map<String, Object>>();
		if (count > 0) {
			List<Trailer> trailerList = list.getList();// 预约信息
			Set<String> userIds = new HashSet<String>();// 用户ids 用来批量查询用户信息
			List<String> trailerIds = new ArrayList<String>();// 预告ids
																// 用户批量查询用户预约
			for (Trailer trailer : trailerList) {
				userIds.add(trailer.getUserId());
				trailerIds.add(trailer.getUuid());
			}
			Map<String, UserVo> userMap = new HashMap<String, UserVo>();
			if(!userIds.isEmpty()){
				List<String> userIdList = new ArrayList<String>();
				userIdList.addAll(userIds);
				List<UserVo> userList = userService.batchUserVo(userIdList);// 批量查询用户信息
				for (UserVo user : userList) {
					userMap.put(user.getUuid(), user);
				}
			}
			List<UserTrailer> treilerList = new ArrayList<UserTrailer>();
			if (trailerIds.size() > 0) {
				treilerList = trailerService.selectUserTrailer(userId, trailerIds);
			}
			Map<String, UserTrailer> trailerMap = new HashMap<String, UserTrailer>();
			for (UserTrailer userTrailer : treilerList) {
				trailerMap.put(userTrailer.getTrailerId(), userTrailer);
			}
			for (Trailer trailer : trailerList) {
				Map<String, Object> map1 = new HashMap<String, Object>();
				map1.put("uuid", trailer.getUuid());
				map1.put("title", ConvertUtil.objectToString(trailer.getTitle(), false));
				map1.put("content", ConvertUtil.objectToString(trailer.getContent(), false));
				map1.put("picUrl", trailer.getPicUrl());
				map1.put("playTime", DateUtil.dateToString(trailer.getPlayTime(), DateUtil.FORMAT_MINUTE_CN));
				UserVo user = userMap.get(trailer.getUserId());
				map1.put("user", userToMap(user, trailerMap.get(trailer.getUuid()) == null ? 0 : 1));
				re.add(map1);
			}
		}
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("list", re);
		reMap.put("totalCount", count);
		return JsonResponse.success(reMap);
	}

	/**
	 * 获取预告信息<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/get", method = RequestMethod.POST)
	public ResponseEntity<Object> get(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String trailerId = body.getString("trailerId");// 预告uuid
		if (StringUtils.isBlank(trailerId)) {
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}
		Trailer trailer = trailerService.findByUuid(trailerId);
		if (trailer == null) {
			return JsonResponse.fail(ErrorCode.ADVERT_NOT_EXIST);
		}
		return JsonResponse.success(trailer);
	}

	/**
	 * 保存或修改预告<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/modify", method = RequestMethod.POST)
	public ResponseEntity<Object> modify(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String trailerId = body.getString("trailerId");// 预告uuid

		trailerService.add(trailerId, body.getString("playTime"), body.getString("title"),
				body.getString("picUrl"), body.getString("content"), body.getString("userId"));
		
		return JsonResponse.success();
	}

	/**
	 * 根据条件查询预告列表<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/conditionList", method = RequestMethod.POST)
	public ResponseEntity<Object> conditionList(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		Integer status = body.getInteger("status");// 状态 0：全部 1：有效 2：失效
		status = status == null ? 0 : status;
		String trailerId = body.getString("trailerId");// 预告Id
		String title = body.getString("title");// 标题
		String userId = body.getString("userId");// 主播ID
		Integer pageNo = body.getInteger("pageNo");// 当前页
		pageNo = pageNo == null ? 1 : pageNo;
		Integer pageSize = body.getInteger("pageSize");// 页数

		Integer count = trailerService.selectTrailerListCount(status, trailerId, title, userId, pageNo, pageSize);
		List<Trailer> list = new ArrayList<Trailer>();
		if (count.intValue() > 0) {
			list = trailerService.selectTrailerList(status, trailerId, title, userId, pageNo, pageSize);
		}
		return JsonResponse.success(new Pages<Trailer>(count, list));
	}

	/**
	 * 删除预告<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public ResponseEntity<Object> delete(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);

		String trailerId = body.getString("trailerId");// 预告ID
		if (StringUtils.isBlank(trailerId)) {
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}
		Trailer trailer = trailerService.findByUuid(trailerId);
		if (trailer == null) {
			return JsonResponse.fail(ErrorCode.ADVERT_NOT_EXIST);
		}
		trailer.setStatus(TrailerStatus.Unavailable.ordinal());// 不可用
		trailerService.save(trailer);
		return JsonResponse.success();
	}

	/**
	 * 添加或取消预约<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<Object> add(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = request.getHeader("userId");
		String uuid = body.getString("uuid");
		if (StringUtils.isBlank(uuid)) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		Integer type = body.getInteger("type");
		type = type == null ? 0 : type;
		// type 0：添加预约 1：取消预约
		if (type.intValue() == 1) {
			Integer status = trailerService.deleteUserTrailer(userId, uuid);
			if (status < 1) {
				return JsonResponse.fail(ErrorCode.NOT_ADVERT);
			}
			return JsonResponse.success();
		}
		// 判断用户是否已预约当前预告
		UserTrailer trailer = trailerService.selectUserTrailer(userId, uuid);
		if (trailer != null) {
			return JsonResponse.fail(ErrorCode.HAS_ADVERT);
		}
		trailerService.saveUserTrailer(userId, uuid);// 添加预告
		return JsonResponse.success();
	}

	/**
	 * user信息转换成map<br>
	 * 
	 * @param user
	 * @return Map<String,Object>
	 */
	private Map<String, Object> userToMap(UserVo user, int isTrailer) {
		Map<String, Object> userMap = new HashMap<String, Object>();
		Map<String, Object> profileMap = new HashMap<String, Object>();
		String uuid = "";
		String nickname = "";
		String avatar = "";
		Integer level = 0;
		if (user != null) {
			uuid = user.getUuid();
			UserProfileVo profile = user.getProfile();
			if (profile != null) {
				nickname = ConvertUtil.objectToString(profile.getNickname(), false);
				avatar = ConvertUtil.objectToString(profile.getAvatar(), false);
				level = ConvertUtil.objectToInt(profile.getLevel(), false);
			}
		}
		userMap.put("uuid", uuid);
		userMap.put("isTrailer", isTrailer);
		profileMap.put("nickname", nickname);
		profileMap.put("avatar", avatar);
		profileMap.put("level", level);
		userMap.put("profile", profileMap);
		return userMap;
	}
}
