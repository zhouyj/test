package com.feedss.manage.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.feedss.manage.entity.ManageVersion;
import com.feedss.manage.service.ManageVersionService;

/**
 * 管理版本
 * 
 * @author 张杰
 *
 */
@Controller
@RequestMapping("/manage/version")
public class ManageVersionController {

	@Autowired
	ManageVersionService manageVersionService;

	Logger logger = Logger.getLogger(getClass());

	@RequestMapping(value = "list", method = { RequestMethod.GET, RequestMethod.POST })
	public String list(String version, String channel, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, HttpSession session, Model model) {
		Map<String, Object> map = manageVersionService.list(version, channel, page, size);
		model.addAllAttributes(map);
		model.addAttribute("version", version);
		model.addAttribute("channel", channel);
		return "/manage/version/list";
	}

	@RequestMapping("add")
	public String toAdd(Model model) {
		List<String> channels = manageVersionService.getChannels();
		model.addAttribute("channels", channels);
		return "/manage/version/addNew";
	}

	@RequestMapping(value = "add", method = { RequestMethod.POST })
	@ResponseBody
	public String add(String[] channelInfos, // 渠道,文件地址
			String[] fileUrls, String update_info) {
		JSONObject json = new JSONObject();
		for (String fileName : channelInfos) {
			String infos = fileName.substring(0, fileName.lastIndexOf("."));
			if (infos.split("_").length != 4) {
				json.put("code", 1);
				json.put("msg", "文件名格式必须是：版本_版本号_更新类型_渠道.apk");
				return json.toJSONString();
			}
		}

		try {
			json = manageVersionService.add(channelInfos, fileUrls, update_info);
		} catch (Exception e) {
			logger.error(e.toString(), e);
			json.put("code", 1);
			json.put("msg", "失败");
		}
		return json.toJSONString();
	}

	@RequestMapping(value = "del", method = { RequestMethod.POST })
	@ResponseBody
	public String del(String uuid) {
		JSONObject json = new JSONObject();
		int row = manageVersionService.updateStatus(uuid, 1);
		if (row > 0) {
			json.put("code", 0);
			json.put("msg", "成功");
		} else {
			json.put("code", 1);
			json.put("msg", "失败");
		}
		return json.toJSONString();
	}

	@RequestMapping(value = "detail", method = { RequestMethod.GET })
	public String detail(String uuid, Model model) {
		ManageVersion mv = manageVersionService.detail(uuid);
		model.addAttribute("mv", mv);
		return "/manage/version/detail";
	}

}
