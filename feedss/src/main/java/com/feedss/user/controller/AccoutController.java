package com.feedss.user.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.util.DateUtil;
import com.feedss.user.entity.Account;
import com.feedss.user.entity.AccountTransaction;
import com.feedss.user.entity.AccountTransaction.AccoutTransactionSourceType;
import com.feedss.user.service.AccountService;

@RestController
public class AccoutController {

	@Autowired
	private AccountService accountService;


	@RequestMapping("user/account/transaction")
	public ResponseEntity<Object> getTransactionList(HttpServletRequest request, @RequestBody String bodyStr,
			@RequestHeader String userId) {		
		JSONObject body = JSONObject.parseObject(bodyStr);

		HashMap<String, Object> data = new HashMap<String, Object>();
		if (StringUtils.isBlank(userId)) {
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}
		Integer pageSize = body.getInteger("pageSize");// 每页显示数量
		pageSize = pageSize == null ? 10 : pageSize;
		String directionStr = body.getString("direction");// 翻页反向 next：下页 pre：上页
		String cursorId = body.getString("cursorId");// 当前游标
		int direction = 1;
		if (StringUtils.isEmpty(directionStr) || directionStr.equals("next")) {
			direction = 2;
		}
		Account account = accountService.getAccountInfo(userId);
		if (account == null) {
			account = accountService.createAccount(userId);
		}
		List<AccountTransaction> result = accountService.selectAccountTransactions(userId, cursorId, pageSize, direction);
		if(result!=null){
			for(AccountTransaction at:result){
				at.setFriendlyTime(DateUtil.dateToString(at.getCreated(), DateUtil.FORMAT_STANDERD));
				if(StringUtils.isEmpty(at.getName())){
					if(StringUtils.isNotEmpty(at.getSourceType())){
						try{
							AccoutTransactionSourceType type = AccoutTransactionSourceType.valueOf(at.getSourceType());
							at.setName(type.getMsg());
						}catch (Exception e) {
							e.printStackTrace();
						}
						
					}else if(at.getBalance()>0){
						at.setName("收入");
					}else if(at.getBalance()<0){
						at.setName("支出");
					}
					
				}
			}
		}
		data.put("list", result);
		return JsonResponse.success(data);
	}
	
	
	@RequestMapping("user/account/getBalance")
	public ResponseEntity<Object> getBalance(HttpServletRequest request, @RequestBody String body,
			@RequestHeader String userId) {
		if (StringUtils.isBlank(userId)) {
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS);
		}
		return JsonResponse.response(accountService.getAccountBalance(userId));
	}

}
