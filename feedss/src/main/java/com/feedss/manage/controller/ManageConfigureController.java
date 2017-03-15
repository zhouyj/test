package com.feedss.manage.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.Constants;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.util.conf.ConfigureUtil;

/**
 * 全局设置
 *
 * @author : by AegisLee on 16/11/17.
 */
@Controller
@RequestMapping("/manage/configure/")
public class ManageConfigureController {
    @Autowired
    ConfigureUtil settingService;

    @RequestMapping(value = "settings", method = {RequestMethod.GET})
    public String setting(Model model) {
        model.addAttribute(Constants.SITE_TITLE, settingService.getConfigureValue(Constants.SITE_TITLE));
        model.addAttribute(Constants.SITE_LOGO_URL, settingService.getConfigureValue(Constants.SITE_LOGO_URL));
        model.addAttribute(Constants.SITE_BANNER_URL, settingService.getConfigureValue(Constants.SITE_BANNER_URL));

        model.addAttribute(Constants.SITE_INTEGRATION_NAME, settingService.getConfigureValue(Constants.SITE_INTEGRATION_NAME));

        model.addAttribute(Constants.APP_LEFTMODEL_NAME, settingService.getConfigureValue(Constants.APP_LEFTMODEL_NAME));
        model.addAttribute(Constants.APP_LEFTMODEL_URL, settingService.getConfigureValue(Constants.APP_LEFTMODEL_URL));
        model.addAttribute(Constants.APP_RIGHTMODEL_NAME, settingService.getConfigureValue(Constants.APP_RIGHTMODEL_NAME));
        model.addAttribute(Constants.APP_RIGHTMODEL_URL, settingService.getConfigureValue(Constants.APP_RIGHTMODEL_URL));
        return "/manage/configure/settings";
    }

    @ResponseBody
    @RequestMapping(value = "save", method = RequestMethod.POST)
    public ResponseEntity<Object> saveSetting(HttpServletRequest request, Model model){
        String siteTitle = request.getParameter("siteTitle");
        String siteLogoUrl = request.getParameter("siteLogoUrl");
        String siteBannerUrl = request.getParameter("siteBannerUrl");

        String siteIntegrationName = request.getParameter("siteIntegrationName");
        String weChatAppId = request.getParameter("weChatAppId");
        String weChatAppSecret = request.getParameter("weChatAppSecret");
        
        String appLeftModelName = request.getParameter(Constants.APP_LEFTMODEL_NAME);
        String appLeftModelUrl = request.getParameter(Constants.APP_LEFTMODEL_URL);
        String appRightModelName = request.getParameter(Constants.APP_RIGHTMODEL_NAME);
        String appRightModelUrl = request.getParameter(Constants.APP_RIGHTMODEL_URL);

        boolean isSuccess = settingService.save(siteTitle, siteLogoUrl, siteBannerUrl, siteIntegrationName,
                                                    weChatAppId, weChatAppSecret, appLeftModelName, appLeftModelUrl, appRightModelName, appRightModelUrl);

//        System.out.print(siteTitle + " ------- " + siteIntegrationName + " --- result: " + isSuccess);


        if (isSuccess)
            return JsonResponse.success(new JSONObject());
        else
            return JsonResponse.fail(ErrorCode.INTERNAL_ERROR);
    }
}
