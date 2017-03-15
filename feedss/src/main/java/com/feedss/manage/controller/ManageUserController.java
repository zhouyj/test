package com.feedss.manage.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.content.entity.Stream;
import com.feedss.content.service.RoomService;
import com.feedss.content.service.StreamService;
import com.feedss.manage.model.ManageUserVo;
import com.feedss.manage.util.Constants;
import com.feedss.user.entity.Role.RoleType;
import com.feedss.user.entity.User;
import com.feedss.user.entity.User.UserStatus;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.UserRoleService;
import com.feedss.user.service.UserService;

/**
 * 用户
 * 
 * @author 张杰
 *
 */
@Controller
@RequestMapping("/manage/user")
public class ManageUserController {

	@Autowired
	UserService userService;
	@Autowired
	StreamService streamService;
	@Autowired
	RoomService roomService;
	@Autowired
	UserRoleService userRoleService;
	@Autowired
	ConfigureUtil configureUtil;

	@RequestMapping(value = "list", method = { RequestMethod.GET, RequestMethod.POST })
	public String list(String uuid, String nickname, String mobile, @RequestParam(defaultValue = "0") int v,
			String field, String direction, @RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size, HttpSession session, Model model) {
		Map<String, Object> data = userService.pageFindUser(uuid, nickname, mobile, field, direction, page, size, v);
		List<ManageUserVo> list = (List<ManageUserVo>) data.get("list");
		for (ManageUserVo u : list) {
			u.setAge(ManageUserVo.getAgeFromBirth(u.getBirthdate()));
		}
		Map<String, Object> map = new HashMap<>();
		map.put("list", list);
		model.addAllAttributes(data);
		model.addAttribute("uuid", uuid);
		model.addAttribute("nickname", nickname);
		model.addAttribute("mobile", mobile);
		model.addAttribute("field", field);
		model.addAttribute("v", v);
		model.addAttribute("direction", direction);
		return "/manage/user/list";
	}

	@RequestMapping(value = "enabled", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String enabled(String userId, HttpSession session) {
		int row = userService.editStatus(userId, User.UserStatus.NORMAL.ordinal());
		JSONObject r_json = new JSONObject();
		if (row > 0) {
			List<Stream> list = streamService.userActiveStream(userId);
			if (list != null && !list.isEmpty()) {
				for (Stream s : list) {
					roomService.anchorClose(s.getUuid(), userId);
				}
			}
			r_json.put("code", ErrorCode.SUCCESS.getCode());
		} else {
			r_json.put("code", ErrorCode.ERROR.getCode());
			r_json.put("msg", ErrorCode.ERROR.getMsg());
		}
		return r_json.toJSONString();
	}

	@RequestMapping(value = "disable", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String disable(String userId, HttpSession session) {
		JSONObject r_json = new JSONObject();
		int row = userService.editStatus(userId, UserStatus.DISABLED.ordinal());
		if (row > 0) {
			r_json.put("code", ErrorCode.SUCCESS.getCode());
		} else {
			r_json.put("code", ErrorCode.ERROR.getCode());
			r_json.put("msg", ErrorCode.ERROR.getMsg());
		}
		return r_json.toJSONString();
	}

	@RequestMapping(value = "removeV", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String removeV(String userId, HttpSession session) {
		JSONObject r_json = new JSONObject();
		int row = userRoleService.removeUserRole(RoleType.V, userId);
		if (row > 0) {
			userService.updateCacheUserVoRoles(userId);
			r_json.put("code", ErrorCode.SUCCESS.getCode());
		} else {
			r_json.put("code", ErrorCode.ERROR.getCode());
			r_json.put("msg", ErrorCode.ERROR.getMsg());
		}
		return r_json.toJSONString();
	}

	@RequestMapping(value = "addHost", method = { RequestMethod.POST })
	@ResponseBody
	public int addHost(String userId, HttpSession session) {
		return userService.addUserRole(userId, RoleType.HOST);
	}

	@RequestMapping(value = "removeHost", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public int removeHost(String userId, HttpSession session) {
		return userService.removeUserRole(userId, RoleType.HOST);
	}

	// 取消管理员
	@RequestMapping(value = "removeAdmin", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public int toRemoveAdmin(String userId, HttpSession session) {
		return userService.removeUserRole(userId, RoleType.ADMIN);
	}

	@RequestMapping(value = "addAdmin", method = { RequestMethod.GET })
	public String toAddAdmin(String userId, HttpSession session, Model model) {
		model.addAttribute("userId", userId);
		return "/manage/user/addAdmin";
	}

	// 添加管理员
	@RequestMapping(value = "addAdmin", method = { RequestMethod.POST })
	@ResponseBody
	public int addAdmin(String userId, String username, String password, HttpSession session) {
		int row = userService.updateUsernameAndPassword(username, DigestUtils.md5Hex(password), userId);
		if (row > 0) {
			userService.addUserRole(userId, RoleType.ADMIN);

		}
		return row;
	}

	@RequestMapping(value = "agreement", method = { RequestMethod.GET, RequestMethod.POST })
	public String showAgreement(Model model, HttpSession session) {
		UserVo user = (UserVo) session.getAttribute(Constants.USER_SESSION);
		String agreement = showAgreement(user);
		model.addAttribute("agreement", agreement);
		return "/manage/user/agreement";
	}

	// 修改用户协议
	@RequestMapping(value = "saveAgreement", method = { RequestMethod.POST })
	@ResponseBody
	public int saveAgreement(String agreement, HttpSession session) {
		UserVo user = (UserVo) session.getAttribute(Constants.USER_SESSION);
		if (StringUtils.isNotEmpty(agreement)) {
			return saveAgreement(agreement, user);
		}
		return -1;
	}

	public String showAgreement(UserVo user) {
		File file = new File(configureUtil.getConfig(com.feedss.base.Constants.AGREEMENT_FILE));
		if (file.exists()) {

			StringBuffer agreement = new StringBuffer();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String tempString = null;
				while ((tempString = reader.readLine()) != null) {
					agreement.append(tempString);
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e1) {
					}
				}
			}
			return agreement.toString();
		}
		return "";
	}

	/**
	 * 修改用户协议
	 * 
	 * @param agreement
	 */
	public int saveAgreement(String agreement, UserVo user) {
		try {
			File file = new File(configureUtil.getConfig(com.feedss.base.Constants.AGREEMENT_FILE));
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(agreement);
			bw.close();
			bw.flush();
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
