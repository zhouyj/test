
package com.feedss.portal.controller;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.JsonData;
import com.feedss.base.util.ValidateCode;
import com.feedss.portal.entity.VerifyCode;
import com.feedss.portal.vo.UserProfileVo;
import com.feedss.user.controller.SmsController.SmsType;
import com.feedss.user.entity.Favorite;
import com.feedss.user.entity.Favorite.FavoriteType;
import com.feedss.user.entity.TimeCard;
import com.feedss.user.entity.TimeCard.TimeCardStatus;
import com.feedss.user.service.AccountService;
import com.feedss.user.service.SmsService;
import com.feedss.user.service.UserFavoriteService;
import com.feedss.user.service.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户相关接口
 * @author Looly
 *
 */
@Slf4j
@Controller
@RequestMapping("portal/user")
public class PortalUserController {
	
	@Autowired
	private UserService userService;
	@Autowired
	private SmsService smsService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private UserFavoriteService userFavoriteService;

	/**
	 * 随机图片
	 */
	@RequestMapping("imageCode")
	public void imageCode(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
		ValidateCode validateCode = new ValidateCode(184, 44);
		VerifyCode imgVc = new VerifyCode(VerifyCode.VerifyCodeKey.IMAGE_VERIFYCODE.name(), validateCode.getCode());

		session.setAttribute(imgVc.getKey(), imgVc);
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");

		ServletOutputStream sos = null;
		try {
			sos = response.getOutputStream();
			validateCode.write(sos);
		} finally {
			if (null != sos) {
				sos.close();
			}
		}
	}

	/**
	 * 发送验证码
	 * 
	 * @param mobile
	 * @param type
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "sendSms", method = { RequestMethod.POST })
	public JSONObject sendSms(HttpSession session, @RequestParam("mobile") String mobile, @RequestParam("type") String type, @RequestParam("verify") String verify) {
		JSONObject result = new JSONObject();
		try {
			VerifyCode.verify(session, verify);
		} catch (Exception e) {
			result.put("code", -1);
			result.put("msg", e.getMessage());
			return result;
		}

		if (StringUtils.isEmpty(mobile)) {
			result.put("code", -1);
			result.put("msg", "电话不能为空");
			return result;
		}
		if (StringUtils.isBlank(type)) {
			result.put("code", -1);
			result.put("msg", "短信类型不能为空不能为空");
			return result;
		}
//		JSONObject data = remoteUserService.sendSms(mobile, SmsType.valueOf(type.toUpperCase()));
		JsonData data = smsService.sendRandom(mobile, SmsType.valueOf(type.toUpperCase()).name());
		return (JSONObject) JSON.toJSON(data);
	}

	/**
	 * 用户名密码登录
	 * 
	 * @param username 用户名
	 * @param pass 密码
	 */
	@ResponseBody
	@RequestMapping(value = "signin", method = { RequestMethod.POST })
	public JSONObject signin(HttpSession session, @RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("verifyCode") String verifyCode) {
		// 检查验证码
		JSONObject result = new JSONObject();
		try {
			VerifyCode.verify(session, verifyCode);
		} catch (Exception e) {
			result.put("code", -1);
			result.put("msg", e.getMessage());
			return result;
		}
		
		//验证是否为点卡
//		result  = userService.checkTimeCard(username, password);
		TimeCard timeCard = accountService.getTimeCard(username, password);
		if (timeCard != null && timeCard.getStatus() == TimeCardStatus.UNACTIVIED.ordinal()) {
			session.setAttribute("serialNumber", username);
			result.put("gotoSignup", true);//给客户端JS的标识
			return result;
		}
		
		if(timeCard==null){
			//点卡不存在，检查用户名密码方式登录
			// 验证登录
//			result = remoteUserService.signinByPwd(username, password);
			JsonData jd = userService.pwdLogin(username, DigestUtils.md5Hex(password), "Web");
			result = (JSONObject) JSON.toJSON(jd);
			if(null == result){
				result = new JSONObject();
				result.put("code", -1);
				result.put("msg", "系统异常，请稍后重试");
				return result;
			}
			int code = result.getIntValue("code");
			if (code == 0) {
				// 登录成功
				JSONObject data = result.getJSONObject("data");
				if(data!=null && data.containsKey("user")){
					session.setAttribute("user", data.getJSONObject("user"));
				}
				
			}
		}

		return result;
	}

	/**
	 * 登出
	 */
	@ResponseBody
	@RequestMapping(value = "signout", method = { RequestMethod.POST })
	public JSONObject signout(HttpSession session) {
		session.removeAttribute("user");
		JSONObject result = new JSONObject();
		result.put("code", 0);
		result.put("msg", "用户登出成功！");
		return result;
	}

	/**
	 * 手机注册
	 * @param pass 密码
	 */
	@ResponseBody
	@RequestMapping(value = "signup", method = {RequestMethod.POST})
	public JSONObject signup(HttpSession session, 
			@RequestParam("verify") String verify,
			@RequestParam("mobile") String mobile, 
			@RequestParam("smscode") String smscode, 
			@RequestParam("password") String password, 
			@RequestParam("password2") String password2){
		
		JSONObject result = new JSONObject();
		//检查验证码（注册提交表单不再验证图形验证码，发送手机短信已经验证过）
//		try {
//			VerifyCode.verify(session, verify);
//		} catch (Exception e) {
//			result.put("code", -1);
//			result.put("msg", e.getMessage());
//			return result;
//		}
		
		//检查是否登录
		if(null != session.getAttribute("user")){
			result.put("code", -1);
			result.put("msg", "当前用户已经登录！");
			return result;
		}
		
		//检查两次密码是否相同
		if(StringUtils.isAnyBlank(password, password2) || false == password.equals(password2)){
			result.put("code", -1);
			result.put("msg", "两次密码输入不同！");
			return result;
		}
		
		result = (JSONObject) JSON.toJSON(userService.registerByMobile(mobile, smscode, DigestUtils.md5Hex(password)));
		if(result.getIntValue("code") == 0){//成功
			JSONObject data = result.getJSONObject("data");
			session.setAttribute("user", data.getJSONObject("user"));
		}
		return result;
	}
	
	/**
	 * 更新密码
	 * @param pass 密码
	 */
	@ResponseBody
	@RequestMapping(value = "updatePassword", method = {RequestMethod.POST})
	public JSONObject updatePassword(HttpSession session, 
			@RequestParam("verify") String verify,
			@RequestParam("oldpassword") String oldPassword,
			@RequestParam("password") String password, 
			@RequestParam("password2") String password2){
		
		JSONObject result = new JSONObject();
		
		JSONObject userJson = (JSONObject) session.getAttribute("user");
		//检查是否登录
		if(null == userJson){
			result.put("code", -1);
			result.put("msg", "当前用户未登录！");
			return result;
		}
		String userId = userJson.getString("uuid");
		//检查验证码
		try {
			VerifyCode.verify(session, verify);
		} catch (Exception e) {
			result.put("code", -1);
			result.put("msg", e.getMessage());
			return result;
		}
		
		if(StringUtils.isBlank(oldPassword)){
			result.put("code", -1);
			result.put("msg", "原密码错误！");
			return result;
		}
		
		//检查两次密码是否相同
		if(StringUtils.isAnyBlank(password, password2) || false == password.equals(password2)){
			result.put("code", -1);
			result.put("msg", "两次密码输入不同！");
			return result;
		}
		
//		result = remoteUserService.updatePwd(oldPassword, password, password2);
		result = (JSONObject) JSON.toJSON(userService.updatePwd(userId, DigestUtils.md5Hex(oldPassword), DigestUtils.md5Hex(password), DigestUtils.md5Hex(password2)));
		if(result.getIntValue("code") == 0){//成功后退出登录
			result.put("msg", "密码修改成功，请重新登录！");
			session.removeAttribute("user");
		}
		return result;
	}
	
	/**
	 * 重置密码
	 * @param pass 密码
	 */
	@ResponseBody
	@RequestMapping(value = "resetPwd", method = {RequestMethod.POST})
	public JSONObject resetPwd(HttpSession session, 
			@RequestParam("verify") String verify,
			@RequestParam("mobile") String mobile,
			@RequestParam("smsCode") String smsCode,
			@RequestParam("password") String password, 
			@RequestParam("password2") String password2){
		
		JSONObject result = new JSONObject();
		//检查验证码（修改密码提交表单不再验证图形验证码，发送手机短信已经验证过）
//		try {
//			VerifyCode.verify(session, verify);
//		} catch (Exception e) {
//			result.put("code", -1);
//			result.put("msg", e.getMessage());
//			return result;
//		}
		
		if(StringUtils.isAnyBlank(mobile, smsCode)){
			result.put("code", -1);
			result.put("msg", "手机或验证码不能为空！");
			return result;
		}
		
		//检查两次密码是否相同
		if(StringUtils.isAnyBlank(password, password2) || false == password.equals(password2)){
			result.put("code", -1);
			result.put("msg", "两次密码输入不同！");
			return result;
		}
		
//		result = remoteUserService.resetPwd(mobile, smsCode, password, password2);
		result = (JSONObject) JSON.toJSON(userService.resetPwd(mobile, smsCode, password, password2));
		if(result.getIntValue("code") == 0){//成功后退出登录
			result.put("msg", "密码修改成功，请登录！");
			session.removeAttribute("user");
		}
		return result;
	}
	
	/**
	 * 用户名密码注册
	 */
	@ResponseBody
	@RequestMapping(value = "signupByUserPwd", method = {RequestMethod.POST})
	public JSONObject signupByUserPwd(HttpSession session, 
			@RequestParam("username") String username, 
			@RequestParam("password") String password,
			@RequestParam("password2") String password2){
		
		JSONObject result = new JSONObject();
		
		//检查用户名密码
		if(StringUtils.isAnyBlank(username, password)){
			result.put("code", -1);
			result.put("msg", "卡号和密码都不能为空！");
			return result;
		}
		
		//检查session中的点卡
		String serialNumber = (String) session.getAttribute("serialNumber");
		if(StringUtils.isBlank(serialNumber)){
			//拒绝未验证点卡的请求
			result.put("code", -1);
			result.put("msg", "无权访问此页！");
			return result;
		}
		
		//检查是否登录
		if(null != session.getAttribute("user")){
			result.put("code", -1);
			result.put("msg", "当前用户已经登录！");
			return result;
		}
		
		//检查两次密码是否相同
		if(StringUtils.isAnyBlank(password, password2) || false == password.equals(password2)){
			result.put("code", -1);
			result.put("msg", "两次密码输入不同！");
			return result;
		}
		
//		result = remoteUserService.signupByUserName(username, password, serialNumber);
		result = (JSONObject) JSON.toJSON(userService.registerByUsername(username, DigestUtils.md5Hex(password), serialNumber));
		if(result.getIntValue("code") == 0){//成功
			JSONObject data = result.getJSONObject("data");
			session.setAttribute("user", data.getJSONObject("user"));
		}
		return result;
	}
	
	/**
	 * 绑定卡片
	 * @param pass 密码
	 */
	@ResponseBody
	@RequestMapping(value = "bindCard", method = {RequestMethod.POST})
	public JSONObject bindCard(HttpSession session, 
			@RequestParam("verify") String verify,
			@RequestParam("serialNumber") String serialNumber, 
			@RequestParam("password") String password){
		
		JSONObject result = new JSONObject();
		JSONObject userJson = (JSONObject) session.getAttribute("user");
		//检查是否登录
		if(null == userJson){
			result.put("code", -1);
			result.put("msg", "当前用户未登录！");
			return result;
		}
		String userId = userJson.getString("uuid");
		
		//检查验证码
		try {
			VerifyCode.verify(session, verify);
		} catch (Exception e) {
			result.put("code", -1);
			result.put("msg", e.getMessage());
			return result;
		}
		
		//检查卡号密码
		if(StringUtils.isAnyBlank(serialNumber, password)){
			result.put("code", -1);
			result.put("msg", "卡号和密码都不能为空！");
			return result;
		}
		
//		result = remoteUserService.bindTimeCard(serialNumber, password);
		result = (JSONObject) JSON.toJSON(userService.bindTimecard(userId, serialNumber, password));
		//个性化消息
		if(result.getIntValue("code") == 0){
			result.put("msg", "绑定成功！");
		}
		return result;
	}
	
	/**
	 * 绑定手机
	 * @param session {@link HttpSession}
	 * @param mobile 手机号
	 * @param smsCode 验证码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "bindMobile", method = {RequestMethod.POST})
	public JSONObject bindMobile(HttpSession session, @RequestParam("mobile") String mobile, @RequestParam("smsCode") String smsCode){
		JSONObject result = new JSONObject();
		
		JSONObject userJson = (JSONObject) session.getAttribute("user");
		//检查是否登录
		if(null == userJson){
			result.put("code", -1);
			result.put("msg", "当前用户未登录！");
			return result;
		}
		String userId = userJson.getString("uuid");
		
//		result = remoteUserService.bindMobile(mobile, smsCode);
		result = (JSONObject) JSON.toJSON(userService.bindMobile(userId, mobile, smsCode));
		//个性化消息
		if(result.getIntValue("code") == 0){
			result.put("msg", "绑定成功！");
		}
		return result;
	}
	
	/**
	 * 更新个人信息<br>
	 * 同时提供给：<br>
	 * 1、补充个人信息<br>
	 * 2、修改个人信息<br>
	 * 
	 * @param session {@link HttpSession}
	 * @param profile 个人信息对象，通过SpringMVC注入
	 */
	@ResponseBody
	@RequestMapping(value = "updateProfile", method = {RequestMethod.POST})
	public JSONObject updateProfile(HttpSession session, UserProfileVo profile){
		JSONObject result = new JSONObject();
		//检查是否登录
		if(null == session.getAttribute("user")){
			result.put("code", -1);
			result.put("msg", "当前用户未登录！");
			return result;
		}
		
		//检查之前用户信息是否补充过，补充过则跳转到个人页，否则跳转到注册完成页
		JSONObject userJson = (JSONObject) session.getAttribute("user");
		String userId = userJson.getString("uuid");
		boolean isProfileEmpty = userJson.getBooleanValue("profileIsEmpty");
		
		log.debug("UserProfileVo: {}", profile);
//		result = remoteUserService.updateProfile(profile);
		result = (JSONObject) JSON.toJSON(userService.updateProfile(userId, (JSONObject)JSON.toJSON(profile)));
		if(0 == result.getIntValue("code")){
			JSONObject data = result.getJSONObject("data");
			if(null != data && false == data.isEmpty()){
				//更新成功
				session.setAttribute("user", data.getJSONObject("user"));
				session.setAttribute("reward", data.getIntValue("reward"));//获得奖励
			}
		}
		
		result.put("gotoProfile", false == isProfileEmpty);//JS中识别此字段用于选择跳转位置
		return result;
	}
	
//	/**
//	 * 上传头像
//	 * @param profile 个人信息对象，通过SpringMVC注入
//	 */
//	@ResponseBody
//	@RequestMapping(value = "uploadAvatar", method = {RequestMethod.POST})
//	public JSONObject uploadAvatar(HttpSession session, @RequestParam(value ="file") MultipartFile file){
//		JSONObject result = new JSONObject();
//		//检查是否登录
//		if(null == session.getAttribute("user")){
//			result.put("code", -1);
//			result.put("msg", "当前用户未登录！");
//			return result;
//		}
//		
//		return uploadImg(file);
//	}
//	
//	/**
//	 * 上传图片
//	 * @param file
//	 * @return
//	 */
//	public JSONObject uploadImg(MultipartFile file){
//		if(StringUtils.isBlank(tempDirStr)){
//			//默认使用系统临时目录
//			tempDirStr = System.getProperty("java.io.tmpdir");
//		}
//		
//		File tempDir = new File(tempDirStr);
//		if(false == tempDir.exists()){
//			tempDir.mkdirs();
//		}
//		
//		
//		File tempFile = null;
//		FileOutputStream fo = null;
//		try {
//			tempFile = File.createTempFile("portal_upload_", "_"+file.getOriginalFilename() , tempDir);
//			log.debug("Save temp pic file to: [{}]", tempFile.getAbsolutePath());
//			fo = new FileOutputStream(tempFile);
//			fo.write(file.getBytes());
//		} catch (IOException e) {
//		}finally{
//			if(null != fo){
//				try {
//					fo.close();
//				} catch (IOException e) {
//				}
//			}
//		}
//		
//		JSONObject result = new JSONObject();
//		//请求User Center
//		MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
//		params.add("file", new FileSystemResource(tempFile));
//		result = RestTemplateUtil.postForJSON(, params, new ClientHttpRequestInterceptor(){
//			@Override
//			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
//				ClientHttpResponse response = execution.execute(request, body);
//				log.debug(response.toString());
//				return response;
//			}
//		});
//		
//		return result;
//	}
	
	/**
	 * 收藏
	 */
	@RequestMapping(value = "favoriteArticle", method = {RequestMethod.POST})
	@ResponseBody
	public JSONObject favoriteArticle(HttpSession session, @RequestParam("extAttr") String extAttr){
		JSONObject result = new JSONObject();
		//检查是否登录
		JSONObject userJson = (JSONObject) session.getAttribute("user");
		if(null == userJson){
			result.put("code", -1);
			result.put("msg", "当前用户未登录！");
			return result;
		}
		String userId = userJson.getString("uuid");
		
		try {
//			result = remoteUserService.favoriteArticle(JSON.parseObject(extAttr));
			JSONObject ext = JSON.parseObject(extAttr);
			Favorite f = userFavoriteService.add(userId, "article", ext.getString("uuid"), FavoriteType.FAVORITE.name(), ext.toJSONString());
			if(f==null){
				result.put("code", -1);
			}else{
				result.put("code", 0);
			}
		} catch (Exception e) {
			result = new JSONObject();
			result.put("code", -1);
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * 取消收藏
	 */
	@RequestMapping(value = "unFavoriteArticle", method = {RequestMethod.POST})
	@ResponseBody
	public JSONObject unFavoriteArticle(HttpSession session, @RequestParam("articleId") String articleId){
		JSONObject result = new JSONObject();
		JSONObject userJson = (JSONObject) session.getAttribute("user");
		if(null == userJson){
			result.put("code", -1);
			result.put("msg", "当前用户未登录！");
			return result;
		}
		String userId = userJson.getString("uuid");
		
		try {
			int row = userFavoriteService.del(userId, "article", articleId, FavoriteType.FAVORITE.name());
			if(row<=0){
				result.put("code", -1);
			}else{
				result.put("code", 0);
			}
		} catch (Exception e) {
			result = new JSONObject();
			result.put("code", -1);
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
}
