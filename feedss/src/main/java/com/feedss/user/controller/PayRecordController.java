package com.feedss.user.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.JsonResponse;
import com.feedss.user.service.PayRecordService;

/**
 * Created by qinqiang on 2016/8/27.
 */
@RestController
@RequestMapping("pay/record")
public class PayRecordController {
	@Autowired
	private PayRecordService payRecordService;

	@RequestMapping("/list")
	public ResponseEntity<Object> payrecordList(HttpServletRequest request,@RequestBody String body){
		JSONObject jsonBody = JSON.parseObject(body);
		int pageNo = jsonBody.getIntValue("pageNo");
		int pageSize = jsonBody.getIntValue("pageSize");
		String orderNo = jsonBody.getString("orderNo");
		String userId = jsonBody.getString("userId");
		String mobile = jsonBody.getString("mobile");
		String sortDirection = jsonBody.getString("sortDirection");
        HashMap<String,Object> dataMap =
		        payRecordService.getPayRecordList(pageNo,pageSize,orderNo,userId,mobile,sortDirection);
		return JsonResponse.success(dataMap);
	}
}
