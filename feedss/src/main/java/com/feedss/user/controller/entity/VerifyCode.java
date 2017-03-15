package com.feedss.user.controller.entity;

import java.util.concurrent.TimeUnit;

/**
 * @author scorpio
 * @version 1.0.0
 * @email tengzhe.ln@alibaba-inc.com
 */
public class VerifyCode {
	public enum VerifyCodeKey{
		IMAGE_VERIFYCODE   //图形验证码
	}
	
    private String key;
    private String value;
    private int verifyTimes;
    private long expireTime;

    /**
     * @param key
     * @param value
     * @param timeout     过期时间
     * @param verifyTimes 当前可验证次数
     */
    public VerifyCode(String key, String value, long timeout, int verifyTimes) {
        this.key = key;
        this.value = value;
        this.expireTime = System.currentTimeMillis() + timeout;
        this.verifyTimes = verifyTimes;
    }

    /**
     * 验证码,默认验证3次,有效期5分钟
     *
     * @param key
     * @param value
     */
    public VerifyCode(String key, String value) {
        this(key, value, TimeUnit.MINUTES.toMillis(5), 3);
    }

    /**
     * 验证校验码
     *
     * @param value
     * @return
     */
    public boolean verify(String value) {
        if (!isValid()) {
            return false;
        }
        boolean valid = verify0(value);
        if (valid) {
            this.verifyTimes = 0;
        } else {
            this.verifyTimes--;
        }
        return valid;
    }

    /**
     * 验证校验码
     *
     * @param value
     * @return
     */
    public boolean verify0(String value) {
        return this.value != null && this.value.equalsIgnoreCase(value);
    }


    public boolean isValid() {
        return verifyTimes > 0 && expireTime > System.currentTimeMillis();
    }

    public String getKey() {
        return key;
    }


    public String getValue() {
        return value;
    }


    public int getVerifyTimes() {
        return verifyTimes;
    }


    public long getExpireTime() {
        return expireTime;
    }


}
