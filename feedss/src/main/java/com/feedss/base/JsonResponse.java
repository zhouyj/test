package com.feedss.base;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @author bruce.liu
 */
public class JsonResponse {
	
	public static ResponseEntity<Object> success(){
		return response(null, "ok", JsonData.CODE_SUCCESS);
	}

	public static ResponseEntity<Object> success(Object data) {
		return response(data, "ok", JsonData.CODE_SUCCESS);
	}

	public static ResponseEntity<Object> success(Object data, String msg) {
		return response(data, msg, JsonData.CODE_SUCCESS);
	}

	public static ResponseEntity<Object> fail(ErrorCode code) {
		return response(new HashMap<>(), code.getMsg(), code.getCode());
	}
	
	public static ResponseEntity<Object> fail(ErrorCode code, String extMsg){
		return response(new HashMap<>(), code.getMsg() + "，" + extMsg, code.getCode());
	}
	
	public static ResponseEntity<Object> response(Object data, String msg, int code) {
		JsonData jd = new JsonData();
		jd.setData(data);
		jd.setCode(code);
		jd.setMsg(msg);
		return new ResponseEntity<Object>(jd, HttpStatus.OK);
	}

	/**
	 * value为null 不显示
	 * 
	 * @param data
	 * @return
	 */
	public static ResponseEntity<Object> successNonNull(Object data) {
		return responseNonNull(data, "OK", JsonData.CODE_SUCCESS);
	}
	
	public static ResponseEntity<Object> response(JsonData jd){
		return new ResponseEntity<>(jd, HttpStatus.OK);
	}

	public static ResponseEntity<Object> responseNonNull(Object data, String msg, int code) {
		JsonData jd = new JsonData();
		jd.setData(data);
		jd.setCode(code);
		jd.setMsg(msg);
		JSONObject json = removeNull(jd);
		return new ResponseEntity<Object>(json, HttpStatus.OK);
	}

	private static JSONObject removeNull(Object obj) {
		JSONObject json = (JSONObject) JSONObject.toJSON(obj);
		String json_str = JSON.toJSONString(json);
		return JSONObject.parseObject(json_str);

	}

}
