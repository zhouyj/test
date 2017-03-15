package com.feedss.user.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.contact.service.MessageService;
import com.feedss.user.entity.Profile;
import com.feedss.user.entity.UsrProduct;
import com.feedss.user.model.ProductVo;
import com.feedss.user.service.AccountService;
import com.feedss.user.service.ProfileService;
import com.feedss.user.service.UserProductService;
import com.feedss.user.service.UsrProductService;

/**
 * Created by qin.qiang on 2016/8/3 0003.
 */
@RestController
public class ProductController {

	@Autowired
	private UsrProductService productService;

	@Autowired
	private UserProductService userProductService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private ProfileService profileService;
	@Autowired
	MessageService messageService;

	/**
	 * 3.2 礼物列表
	 * 
	 * @return
	 */
	@RequestMapping(value = "user/gifts", method = RequestMethod.POST)
	public ResponseEntity<Object> getGiftsList(HttpServletRequest request, @RequestHeader String userId) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		List<ProductVo> productVos = productService.getProductsByType(UsrProduct.ProductType.GIFT.name());
		if (productVos.size() > 0) {
			data.put("list", productVos);
		}
		data.put("balance", accountService.getAccountInfo(userId).getBalance());
		return JsonResponse.success(data);
	}

	/**
	 * 3.3 我的礼物
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "user/myGifts", method = RequestMethod.POST)
	public ResponseEntity<Object> getMyGiftsList(HttpServletRequest request, @RequestBody String body,
			@RequestHeader("userId") String userId) {
		JSONObject jsonBody = JSON.parseObject(body);
		if (StringUtils.isNotBlank(jsonBody.getString("userId"))) {
			userId = jsonBody.getString("userId");
		}
		HashMap<String, Object> data = new HashMap<String, Object>();
		List<HashMap<String, Object>> productVos = userProductService.getProductsByUserId(userId);
		if (productVos.size() > 0) {
			data.put("list", productVos);
		}
		return JsonResponse.success(data);
	}

	/**
	 * 3.4 赠送礼物
	 * 
	 * @return
	 */
	@RequestMapping(value = "user/product/giveGift", method = RequestMethod.POST)
	public ResponseEntity<Object> giveGift(HttpServletRequest request, @RequestBody String body,
			@RequestHeader String userId, @RequestHeader String userToken) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		JSONObject jsonBody = JSON.parseObject(body);
		String toUserId = jsonBody.getString("toUserId");
		String productId = jsonBody.getString("productId");
		String groupId = jsonBody.getString("groupId");

		int num = jsonBody.getIntValue("num");
		if (StringUtils.isBlank(toUserId) || StringUtils.isBlank(productId) || num <= 0) {
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}

		ErrorCode result = productService.giveGift(userId, toUserId, productId, num);
		if (result == ErrorCode.SUCCESS) { // 赠送成功
			sendGiftGroupMsg(groupId, userId, productId, num);// 广播送礼物消息
			data.put("balance", accountService.getAccountInfo(userId).getBalance());
			return JsonResponse.success(data);
		} else {
			return JsonResponse.fail(result);
		}
	}

	void sendGiftGroupMsg(String groupId, String userId, String productId, int num) {
		UsrProduct product = productService.getProductById(productId);
		Profile userProfile = profileService.findByUserId(userId);
		String nickname = userProfile.getNickname();

		// 组织userInfo数据
		JSONObject userInfo = new JSONObject();
		userInfo.put("uuid", userId);
		userInfo.put("nickname", nickname);
		userInfo.put("avatar", userProfile.getAvatar());
		// 组织ext数据
		Map<String, Object> ext = new HashMap<>();
		ext.put("accountId", userId);
		ext.put("groupId", groupId);
		ext.put("messageSource", "SendGift");
		ext.put("text", nickname + "给主播送了" + product.getName() + "X" + num);
		ext.put("userInfo", userInfo);
		ext.put("giftPic", product.getPic());

		messageService.sendGroupMessage(groupId, "", ext, null);
	}
}
