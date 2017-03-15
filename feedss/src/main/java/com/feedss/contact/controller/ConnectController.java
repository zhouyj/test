package com.feedss.contact.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.contact.controller.model.AccountParam;
import com.feedss.contact.controller.model.GroupParam;
import com.feedss.contact.service.QCloudService;


@RestController
@RequestMapping("/connect")
public class ConnectController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private QCloudService qcloudService;

    /**
     * 获取userSig
     *
     * @param accountParam
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/qcloudUserSig", method = RequestMethod.POST)
    public ResponseEntity<Object> qcloudUserSig(HttpServletRequest request, @RequestBody String accountParamStr) {
    	AccountParam accountParam = JSONObject.parseObject(accountParamStr, AccountParam.class);
        if (accountParam == null || StringUtils.isEmpty(accountParam.getAccountId())) {
            return JsonResponse.fail(ErrorCode.INVALIDPARAMETERS);
        }
        String userSig = null;
        try {
            userSig = qcloudService.getUserSig(accountParam.getAccountId());
        } catch (Exception e) {
            logger.error("qcloud user sig", e);
        }
        if (StringUtils.isEmpty(userSig)) {
            return JsonResponse.fail(ErrorCode.INTERNALERROR);
        }
        Map<String, String> result = new HashMap<String, String>();
        result.put("userSig", userSig);
        return JsonResponse.success(result);
    }

    /**
     * 获取usersig并导入账号到云平台
     *
     * @param accountParam
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/qcloudImportUser", method = RequestMethod.POST)
    public ResponseEntity<Object> qcloudImportAccount(HttpServletRequest request, @RequestBody String accountParamStr) {
    	AccountParam accountParam = JSONObject.parseObject(accountParamStr, AccountParam.class);
        if (accountParam == null || StringUtils.isEmpty(accountParam.getAccountId())) {
            return JsonResponse.fail(ErrorCode.INVALIDPARAMETERS);
        }
        String userSig = null;
        try {
            userSig = qcloudService.qcloudImportAccount(accountParam.getAccountId(), accountParam.getNickname(), accountParam.getPhoto());
        } catch (Exception e) {
            logger.error("qcloud import user", e);
        }
        if (StringUtils.isEmpty(userSig)) {
            return JsonResponse.fail(ErrorCode.INTERNALERROR);
        }
        Map<String, String> result = new HashMap<String, String>();
        result.put("userSig", userSig);
        return JsonResponse.success(result);
    }

    @ResponseBody
    @RequestMapping(value = "/uploadUserInfo", method = RequestMethod.POST)
    public ResponseEntity<Object> uploadUserInfo(HttpServletRequest request, @RequestBody String msgStr) {
        GroupParam param = JSON.parseObject(msgStr, GroupParam.class);
        boolean isSuccess = qcloudService.setUserInfo(param.getAccountId(), param.getNickname(), param.getPhoto());
        if (isSuccess)
            return JsonResponse.success();
        else
            return JsonResponse.fail(ErrorCode.INTERNALERROR);
    }


}
