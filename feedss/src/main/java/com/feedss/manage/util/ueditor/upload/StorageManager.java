package com.feedss.manage.util.ueditor.upload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.feedss.base.util.RestTemplateUtil;
import com.feedss.manage.common.UserClientHttpRequestInterceptor;
import com.feedss.manage.util.ueditor.define.AppInfo;
import com.feedss.manage.util.ueditor.define.BaseState;
import com.feedss.manage.util.ueditor.define.FileType;
import com.feedss.manage.util.ueditor.define.State;
import com.feedss.user.model.UserVo;

public class StorageManager {
	public static final int BUFFER_SIZE = 8192;
	
	public static String baseUrl;
	public static String uploadDirPrefix;

	public static State saveBinaryFile(byte[] data, String path) {
		return new BaseState(false, AppInfo.IO_ERROR);
	}

	public static State saveFileByInputStream(InputStream is, String path, UserVo userVo) {
		State state = null;

		File tmpFile = getTmpFile();

		byte[] dataBuf = new byte[ 2048 ];
		BufferedInputStream bis = new BufferedInputStream(is, StorageManager.BUFFER_SIZE);

		try {
			BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream(tmpFile), StorageManager.BUFFER_SIZE);

			int count = 0;
			while ((count = bis.read(dataBuf)) != -1) {
				bos.write(dataBuf, 0, count);
			}
			bos.flush();
			bos.close();

			state = saveTmpFile(tmpFile, path, userVo);

			if (!state.isSuccess()) {
				tmpFile.delete();
			}

			return state;
		} catch (IOException e) {
		}
		return new BaseState(false, AppInfo.IO_ERROR);
	}

	public static State saveFileByInputStream(InputStream is, String path, long maxSize, UserVo userVo) {
		State state = null;

		File tmpFile = getTmpFile();

		byte[] dataBuf = new byte[ 2048 ];
		BufferedInputStream bis = new BufferedInputStream(is, StorageManager.BUFFER_SIZE);

		try {
			BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream(tmpFile), StorageManager.BUFFER_SIZE);

			int count = 0;
			while ((count = bis.read(dataBuf)) != -1) {
				bos.write(dataBuf, 0, count);
			}
			bos.flush();
			bos.close();

			if (tmpFile.length() > maxSize) {
				tmpFile.delete();
				return new BaseState(false, AppInfo.MAX_SIZE);
			}

			state = saveTmpFile(tmpFile, path, userVo);

			if (!state.isSuccess()) {
				tmpFile.delete();
			}

			return state;
		} catch (IOException e) {
		}
		return new BaseState(false, AppInfo.IO_ERROR);
	}

	private static File getTmpFile() {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		String tmpFileName = (Math.random() * 10000 + "").replace(".", "");
		return new File(tmpDir, tmpFileName);
	}

	private static State saveTmpFile(File tmpFile, String path, UserVo userVo) {
		State state = null ;
		String key = uploadDirPrefix + getFileName(path);

		MultiValueMap<String, Object> vars = new LinkedMultiValueMap<>();
		vars.add("file", new FileSystemResource(tmpFile));

		Logger.getRootLogger().error(tmpFile.getAbsolutePath());

		String data = RestTemplateUtil.postRequest(baseUrl + "/" + uploadDirPrefix,
				vars, new UserClientHttpRequestInterceptor(userVo, null));

		Logger.getRootLogger().error("response data : ---" + data);
		if (data == null) {
			state = new BaseState(false, AppInfo.IO_ERROR);
		}else {
			JSONObject entity = JSONObject.parseObject(data, JSONObject.class);
			if (null != entity && entity.getInteger("code") == 0){
				Map<String, List<String>> imgMap = JSONObject.parseObject(entity.getJSONObject("data").toJSONString(),
						new TypeReference<Map<String, List<String>>>() {
				});
				List<String> images = imgMap.get("files");
				if (!images.isEmpty()){
					String fileUrl = images.get(0);
					state = new BaseState(true);
					state.putInfo("size", tmpFile.length());
					state.putInfo("title", key);
					state.putInfo("url", fileUrl);
				}
			}
		}

		if (null == state){
			state = new BaseState(false, AppInfo.IO_ERROR);
		}
		return state;
	}
	
	private static String getFileName(String fileName) {
		String suffix =  FileType.getSuffixByFilename(fileName);
		return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())+ (int)(Math.random()*9000 +1000) + suffix;
	}

	public static State uploadImg(MultipartFile file){

		State state = new BaseState(false, AppInfo.IO_ERROR);
		String tempDirStr = System.getProperty("java.io.tmpdir");

		File tempDir = new File(tempDirStr);
		if(!tempDir.exists()){
			tempDir.mkdirs();
		}

		File tempFile = null;
		FileOutputStream fo = null;

		String prefix = "portal_upload_";
		String fileName = file.getOriginalFilename();
		try {
			tempFile = File.createTempFile(prefix, fileName, tempDir);
			Logger.getRootLogger().error("Save temp pic file to: [{}]" + tempFile.getAbsolutePath());
			fo = new FileOutputStream(tempFile);
			fo.write(file.getBytes());
		} catch (IOException e) {
		}finally{
			if(null != fo){
				try {
					fo.close();
				} catch (IOException e) {
				}
			}
		}

		JSONObject result = new JSONObject();
		//请求User Center
		MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		params.add("file", new FileSystemResource(tempFile));
		result = com.feedss.portal.util.RestTemplateUtil.postForJSON(baseUrl + "/" + uploadDirPrefix,
				params, new ClientHttpRequestInterceptor(){
			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
				ClientHttpResponse response = execution.execute(request, body);
				Logger.getRootLogger().error(response.toString());
				return response;
			}
		});

		if (null != result && result.getInteger("code") == 0){
			Map<String, List<String>> imgMap = JSONObject.parseObject(result.getJSONObject("data").toJSONString(),
					new TypeReference<Map<String, List<String>>>() {
					});
			if (!imgMap.isEmpty()){
				String fileUrl = imgMap.get("files").get(0);
				state = new BaseState(true);
				state.putInfo("size", tempFile.length());
				state.putInfo("title", prefix + fileName);
				state.putInfo("url", fileUrl);

				Logger.getRootLogger().error(fileUrl);
			}
		}

		return state;
	}
}
