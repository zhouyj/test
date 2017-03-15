package com.feedss.content.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.Pages;
import com.feedss.base.util.ConvertUtil;
import com.feedss.base.util.DateUtil;
import com.feedss.content.entity.Banner;
import com.feedss.content.entity.Stream;
import com.feedss.content.entity.Stream.StreamType;
import com.feedss.content.service.BannerService;
import com.feedss.content.service.StreamService;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.UserService;

/**
 * 首页API<br>
 * 
 * @author wangjingqing
 * @date 2016-07-27
 */
@Controller
@RequestMapping("/home")
public class HomeController {

	@Autowired
	private BannerService bannerService;
	@Autowired
	private StreamService streamService;
	@Autowired
	private UserService userService;
	
	/**
	 * 根据分类及状态查询直播流
	 * 
	 * @param userId
	 * @param categoryId
	 * @param status
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/conf", method = RequestMethod.POST)
	public ResponseEntity<Object> systemConfig(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		Map<String, Object> reMap = new HashMap<String, Object>();
		
		return JsonResponse.success(reMap);
	}
	
	/**
	 * 根据分类及状态查询直播流
	 * 
	 * @param userId
	 * @param categoryId
	 * @param status
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/streamsAndHosts", method = RequestMethod.POST)
	public ResponseEntity<Object> streamsAndHosts(HttpServletRequest request, @RequestBody String bodyStr, @RequestHeader("userId") String userId) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		Integer streamSize = body.getInteger("streamSize");// 每页显示数量
		streamSize = streamSize == null ? 10 : streamSize;
		Integer userSize = body.getInteger("userSize");// 每页显示数量
		userSize = userSize == null ? 10 : userSize;
		
		List<Stream> streams = streamService.getStreamsByCategoryOrderByTime(null, null, streamSize, 1);
		List<Map<String, Object>> contentList = streamService.getStreams(streams, false);
		
		List<UserVo> userList = userService.getHostList(userId, null, userSize, 1);
		
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("contentList", contentList);
		reMap.put("userList", userList);
		int count = new Random().nextInt(200);
		reMap.put("onlineCount", count + 100);// 在线人数(100~300随机数)
		return JsonResponse.success(reMap);
	}

	/**
	 * banner<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/banner", method = RequestMethod.POST)
	public ResponseEntity<Object> banner(HttpServletRequest request, @RequestBody String bodyStr) {
		List<Banner> banners = bannerService.selectBannerByLocation(Banner.BannerLocation.HomePageBanner.toString());
		List<Map<String, Object>> re = new ArrayList<Map<String, Object>>();
		for (Banner banner : banners) {
			Map<String, Object> map1 = new HashMap<String, Object>();
			map1.put("uuid", banner.getUuid());// banner唯一标示
			map1.put("title", ConvertUtil.objectToString(banner.getTitle(), false));// 标题
			map1.put("picUrl", ConvertUtil.objectToString(banner.getPicUrl(), false));// 商品图片

			Integer type = Integer.valueOf(banner.getType());// 类型
			StringBuilder builder = new StringBuilder("");
			if (type.intValue() == 1 || type.intValue() == 0) {// 预告和直播
				builder.append("时间：").append(DateUtil.dateToString(banner.getPlayStart(), DateUtil.FORMAT_DAY_CN));// 时间
				if (banner.getPlayEnd() != null) {
					builder.append("至").append(DateUtil.getDay(banner.getPlayEnd())).append("日");
				}
			}
			map1.put("describe", builder.toString());// 描述
			map1.put("content", bannerContent(type, banner.getContent()));// 内容
			map1.put("type", type);// 0:预告 1：直播 2：回放 3：空间 4:H5页面
			re.add(map1);
		}
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("list", re);
		reMap.put("totalCount", re.size());
		return JsonResponse.success(reMap);
	}

	/**
	 * 添加banner<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/bannerAdd", method = RequestMethod.POST)
	public ResponseEntity<Object> bannerAdd(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String picUrl = body.getString("picUrl");
		String type = body.getString("type");
		String title = body.getString("title");
		String content = body.getString("content");
		String bannerId = body.getString("bannerId");
		String location = body.getString("location");
		Banner banner = new Banner();
		if (StringUtils.isNotBlank(bannerId)) {
			banner = bannerService.selectByUUID(bannerId);
			if (banner == null) {
				return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS); // TODO
			}
		} else {
			Integer sort = bannerService.selectMaxSort();
			sort = sort == null ? 0 : sort;
			banner.setSort(sort + 1);

			String sortNew = body.getString("sort");
			if (null != sortNew && StringUtils.isNumeric(sortNew)) {
				sort = Integer.valueOf(sortNew);
				banner.setSort(sort);
			}

			banner.setStart(new Date());
			banner.setPlayStart(new Date());
			banner.setPlayEnd(new Date());
			banner.setEnd(new Date());
			banner.setStatus(1);
			banner.setCreated(new Date());
		}
		banner.setPicUrl(picUrl);
		banner.setType(type);
		banner.setTitle(title);
		banner.setContent(content);
		banner.setLocation(location);
		bannerService.save(banner);
		return JsonResponse.success();
	}

	/**
	 * 获取banner<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/bannerGet", method = RequestMethod.POST)
	public ResponseEntity<Object> bannerGet(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String bannerId = body.getString("bannerId");
		Banner banner = bannerService.selectByUUID(bannerId);
		return JsonResponse.success(banner);
	}

	/**
	 * 获取banner<br>
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/bannerList", method = RequestMethod.POST)
	public ResponseEntity<Object> get(HttpServletRequest request) {
		List<Banner> banners = bannerService.selectBanner();
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("totalCount", banners.size());
		reMap.put("list", banners);
		return JsonResponse.success(reMap);
	}

	/**
	 * 删除banner<br>
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/bannerDel", method = RequestMethod.POST)
	public ResponseEntity<Object> delete(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String bannerId = body.getString("bannerId");
		if (bannerId == null) {
			return JsonResponse.fail(ErrorCode.INTERNAL_FAILURE);
		}
		if(bannerService.delete(bannerId)){
			return JsonResponse.success();
		}
		return JsonResponse.fail(ErrorCode.INTERNAL_FAILURE);
		
	}

	/**
	 * 修改排序<br>
	 */
	@ResponseBody
	@RequestMapping(value = "/bannerSort", method = RequestMethod.POST)
	public ResponseEntity<Object> bannerSort(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);
		String sort = body.getString("sort");
		String bannerId = body.getString("bannerId");
		bannerService.updateSort(bannerId, sort);
		return JsonResponse.success();
	}


	/**
	 * banner内容封装<br>
	 * 
	 * @param type
	 * @param content
	 */
	private String bannerContent(Integer type, String content) {
		String streamId = ""; // 流UUID
		int isVR = 0; // 是否是VR
		int isSecret = 0; // 是否是已加密
		String playUrl = ""; // 直播URL
		String playbackUrl = ""; // 回放URL
		String userId = ""; // 用户UUID
		String trailerId = ""; // 预告UUID
		String url = ""; // H5 URL

		if (type.intValue() == 0) {// 预告
			trailerId = content;
		} else if (type.intValue() == 1 || type.intValue() == 2) {// 直播和回放
			Stream stream = streamService.selectStream(content);
			streamId = content;
			if (stream != null) {
				playUrl = ConvertUtil.objectToString(stream.getPlayUri(), false);
				playbackUrl = ConvertUtil.objectToString(stream.getPlaybackUri(), false);
				isSecret = StringUtils.isBlank(stream.getSecret()) ? 0 : 1;
				isVR = stream.getType().equals(StreamType.VR.toString()) ? 1 : 0;
			}
		} else if (type.intValue() == 3) {// 空间
			userId = content;
		} else if (type.intValue() == 4) {// H5
			url = content;
		}
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("streamId", streamId);
		reMap.put("isVR", isVR);
		reMap.put("isSecret", isSecret);
		reMap.put("playUrl", playUrl);
		reMap.put("playbackUrl", playbackUrl);
		reMap.put("userId", userId);
		reMap.put("trailerId", trailerId);
		reMap.put("url", url);
		return JSONObject.toJSONString(reMap);
	}

	/**
	 * 预告API<br>
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "/streamList", method = RequestMethod.POST)
	public ResponseEntity<Object> trailer(HttpServletRequest request, @RequestBody String bodyStr) {
		JSONObject body = JSONObject.parseObject(bodyStr);

		Integer pageNo = body.getInteger("pageNo");// 当前页
		pageNo = (pageNo == null || pageNo.intValue() == 0) ? 0 : --pageNo;
		
		Integer pageSize = body.getInteger("pageSize");// 每页显示数量
		pageSize = pageSize == null ? 10 : pageSize;
		String userId = body.getString("userId");
		String categoryId = body.getString("categoryId");
		String title = body.getString("title");
		int status = body.getIntValue("status");

		Pages<Stream> list = streamService.findStreams(status, userId, categoryId, title, pageNo, pageSize);
		Integer count = list.getTotalCount();
		List<Map<String, Object>> re = new ArrayList<Map<String, Object>>();
		if (count > 0) {
			List<Stream> streamList = list.getList();
			re = streamService.getStreams(streamList, false);
		}
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("list", re);
		reMap.put("totalCount", count);
		return JsonResponse.success(reMap);
	}
}
