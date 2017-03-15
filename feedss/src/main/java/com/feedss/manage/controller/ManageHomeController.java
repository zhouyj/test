package com.feedss.manage.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.manage.util.Constants;
import com.feedss.user.entity.User;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.UserService;
/**
 * 
 * @author 张杰
 *
 */
@RequestMapping("")
@Controller
public class ManageHomeController {
	
	@Autowired
	private UserService userService;
	
	@RequestMapping("")
	public String index(){
		 return "redirect:/login"; 
	}
	
	@RequestMapping(value="login",method=RequestMethod.GET) 
	public String toLogin(){
		return "/login";
	}
	
	@RequestMapping(value="loginOut",method=RequestMethod.GET) 
	public String loginOut(HttpSession session){
		session.removeAttribute(Constants.USER_SESSION);
		return "/login";
	}
	
	
	@RequestMapping(value="login",method=RequestMethod.POST)
	@ResponseBody
	public String toLogin(HttpServletRequest request,HttpSession session,String loginName,String password){
		JSONObject r_json=new JSONObject();
		User user = userService.getByUserNameAndMobile(loginName);
		
//		Map<String, Object> map=userService.checkLogin(loginName, password);
//		UserVo u=(UserVo)map.get("user");
		
		if(user==null || !user.getPassword().equalsIgnoreCase(DigestUtils.md5DigestAsHex(password.getBytes()))){
			r_json.put("code", 1);
			r_json.put("msg","登录错误");
			
		}else{
			UserVo userVo = userService.getUserInfoWithToken(user, "web");
			if (userVo == null) {
				r_json.put("code", ErrorCode.USERNAME_OR_PASSWORD_ERROR);
				r_json.put("msg","登录错误");
				return r_json.toJSONString();
			}
			
			if (userVo.getStatus() == 1) {
				r_json.put("code", ErrorCode.USER_FORBIDDEN);
				r_json.put("msg","用户被禁用");
				return r_json.toJSONString();
			}
			List<HashMap<String,String>> roles=userVo.getRoles();
			if(roles!=null&&roles.size()>0){
				for(HashMap<String,String> m:roles){
					String code=m.get("code");
					if("0002".equals(code)){
						session.setAttribute(Constants.USER_SESSION, userVo);
						r_json.put("code", 0);
						r_json.put("msg","登录成功");
						return r_json.toJSONString();
					}
				}
			}
		}
		return r_json.toJSONString();
	}
	
	
}
