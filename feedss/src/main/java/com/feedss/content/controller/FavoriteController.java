package com.feedss.content.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.content.entity.Stream;
import com.feedss.content.service.FavoriteService;
import com.feedss.content.service.StreamService;

/**
 * 购物车API<br>
 * 
 * @author wangjingqing
 * @date 2016-07-27
 */
@Controller
@RequestMapping("/favorite")
public class FavoriteController {

	@Autowired
	private StreamService streamService;
	@Autowired
	private FavoriteService favoriteService;

	/**
	 * 添加<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<Object> add(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = request.getHeader("userId");
		String streamId = body.getString("streamId");
		if (StringUtils.isBlank(streamId)) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		Stream stream = streamService.selectStream(streamId);
		if (stream == null) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		String productId = body.getString("productId");
		if (StringUtils.isBlank(productId)) {
			return JsonResponse.fail(ErrorCode.CHOOSE_ONE_PRODUCT);
		}
		boolean status = favoriteService.addFavorite(userId, productId);
		if (!status) {
			return JsonResponse.fail(ErrorCode.INTERNAL_FAILURE);
		}
		return JsonResponse.success();
	}

	/**
	 * 删除<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public ResponseEntity<Object> delete(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = request.getHeader("userId");
		String productId = body.getString("productId");
		if (StringUtils.isBlank(productId)) {
			return JsonResponse.fail(ErrorCode.VIDEO_NOT_EXISTED);
		}
		boolean status = favoriteService.deleteFavorite(userId, productId);
		if (!status) {
			return JsonResponse.fail(ErrorCode.INTERNAL_FAILURE);
		}
		return JsonResponse.success();
	}

	/**
	 * 列表<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Object get(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String userId = request.getHeader("userId");
		String cursorId = body.getString("cursorId");
		Integer pageSize = body.getInteger("pageSize");
		pageSize = pageSize == null ? 20 : pageSize;

		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("list", favoriteService.getFavorites(userId, cursorId, pageSize));
		return JsonResponse.success(reMap);
	}
}
