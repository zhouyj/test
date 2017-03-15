package com.feedss.user.controller;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.feedss.base.Constants;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.util.conf.ConfigureUtil;

/**
 * Created by qin.qiang on 2016/5/29 0029.
 */
@RestController
@RequestMapping("image")
public class AjaxImgUploadController {

	private static final Logger logger = LoggerFactory.getLogger(AjaxImgUploadController.class);

	@Autowired
	private ConfigureUtil configureUtil;

	/***
	 * 接口6.1 图片上传
	 * 
	 * @param file
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "upload")
	@ResponseBody
	public ResponseEntity<Object> upload(@RequestParam(value = "file", required = false) MultipartFile[] file,
			HttpServletRequest request, HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		String os = System.getProperty("os.name");
		String filePath = configureUtil.getConfig(Constants.IMAGE_PATH);
		if (os.toLowerCase().indexOf("windows") >= 0) {
			filePath = "D:/img/";
		}
		if (!filePath.endsWith(File.separator)) {
			filePath = filePath + File.separator;
		}
		String currentDate = new DateTime().toString("YYYYMMdd");
		filePath = filePath + currentDate + File.separator;
		logger.info("img upload start ...");
		logger.info("filepath : " + filePath);
		LinkedList<String> imgs = new LinkedList<String>();
		if (file.length == 0) {
			return JsonResponse.fail(ErrorCode.UPLOADFILE_EMPTY);
		}
		for (MultipartFile multipartFile : file) {
			if (StringUtils.isBlank(multipartFile.getOriginalFilename())) {
				return JsonResponse.fail(ErrorCode.UPLOADFILE_EMPTY);
			}
			String fileName = getFileName(multipartFile.getOriginalFilename());
			File targetFile = new File(filePath, fileName);
			checkDir(filePath);
			try {
				multipartFile.transferTo(targetFile);
			} catch (Exception e) {
				logger.error("img upload error", e);
				return JsonResponse.fail(ErrorCode.UPLOAD_ERROR);
			}
			imgs.add(configureUtil.getConfig(Constants.IMAGE_HOST) + currentDate + File.separator + fileName);
		}
		HashMap<String, List<String>> result = new HashMap<String, List<String>>();
		result.put("images", imgs);
		return JsonResponse.success(result, "上传成功");
	}

	public void checkDir(String desDir) {
		File dir = new File(desDir);
		if (!dir.exists()) {
			logger.info("dir not exist..." + dir.getAbsolutePath());
			try{
				boolean flag = dir.mkdir();
				logger.info("mkdir flag = " + flag + ", dir = " + dir.getAbsolutePath());
			}catch (Exception e) {
				logger.error("mkdir error", e);
			}
			
		}
	}

	public String getFileName(String orgName) {
		UUID uuid = UUID.randomUUID();
		String suffix = orgName.substring(orgName.lastIndexOf("."));
		return uuid.toString() + suffix;
	}
}
