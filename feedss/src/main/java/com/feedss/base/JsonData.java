package com.feedss.base;

import java.util.HashMap;

/**
 * @author bruce.liu
 */
public class JsonData {

	public static final int CODE_SUCCESS = 0;

	private String msg;
	private Object data;
	private int code;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
	public JsonData(ErrorCode errorCode){
		this.code = errorCode.getCode();
		this.msg = errorCode.getMsg();
	}
	public JsonData(ErrorCode errorCode, String msg){
		this.code = errorCode.getCode();
		this.msg = msg;
	}
	public JsonData(Object data){
		this.code = CODE_SUCCESS;
		this.data = data;
	}
	public JsonData(){
	}
	
	public static JsonData fail(ErrorCode errorCode){
		return new JsonData(errorCode);
	}
	public static JsonData fail(ErrorCode errorCode, String msg){
		return new JsonData(errorCode, msg);
	}
	public static JsonData successNonNull(Object data){
		return new JsonData(data);
	}
	public static JsonData success(){
		JsonData jd = new JsonData();
		jd.setCode(CODE_SUCCESS);
		return jd;
	}
	public static JsonData success(String msg){
		JsonData jd = new JsonData();
		jd.setCode(CODE_SUCCESS);
		jd.setMsg(msg);
		return jd;
	}
	public void addData(String key, Object val) {
        if(data == null){
            data = new HashMap<String, Object>();
        }
        if(data instanceof HashMap) {
            ((HashMap<String, Object>) data).put(key, val);
        }
    }
}
