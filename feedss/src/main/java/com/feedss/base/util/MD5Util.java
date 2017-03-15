package com.feedss.base.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * MD5加密<br>
 * @author wangjingqing
 * @date 2016-8-20
 */
public class MD5Util {
	
	static Log logger = LogFactory.getLog(MD5Util.class);
	
	private static  MessageDigest md;
	static{
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			logger.info("初始化MD5失败...", e);
		}
	}
	
	// 全局数组
    private final static String[] strDigits = { "0", "1", "2", "3", "4", "5","6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

    // 返回形式为数字跟字符串
    private static String byteToArrayString(byte bByte) {
        int iRet = bByte;
        if (iRet < 0) {
            iRet += 256;
        }
        int iD1 = iRet / 16;
        int iD2 = iRet % 16;
        return strDigits[iD1] + strDigits[iD2];
    }

    // 转换字节数组为16进制字串
    private static String byteToString(byte[] bByte) {
        StringBuffer sBuffer = new StringBuffer();
        for (int i = 0; i < bByte.length; i++) {
            sBuffer.append(byteToArrayString(bByte[i]));
        }
        return sBuffer.toString();
    }
    /**
     * MD5加密<br>
     * @param strObj
     * @return String
     */
    public static String MD5String(String strObj) {
        String resultString = null;
        byte[] date;
        synchronized (strObj) {
        	date = md.digest(strObj.getBytes());
		}
        resultString = byteToString(date);
        return resultString;
    }
}
