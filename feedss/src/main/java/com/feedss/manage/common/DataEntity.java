package com.feedss.manage.common;

import java.io.Serializable;

public class DataEntity<T> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9037178027118089456L;

	private Integer code;
	
	private String msg;
	
	private T data;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "DataEntity [code=" + code + ", msg=" + msg + ", data=" + data + "]";
	}
	
	
}
