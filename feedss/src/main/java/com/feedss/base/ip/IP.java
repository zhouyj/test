package com.feedss.base.ip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * IP地址库
 * @author Looly
 *
 */
@Slf4j
public class IP {
	
	public static boolean enableFileWatch = false;

	private static int offset;
	private static int[] index = new int[256];
	private static ByteBuffer dataBuffer;
	private static ByteBuffer indexBuffer;
	private static Long lastModifyTime = 0L;
	private static File ipFile;
	private static ReentrantLock lock = new ReentrantLock();
	
	static{
		try {
			IP.load(ResourceUtils.getFile("classpath:17monipdb.dat"));
		} catch (FileNotFoundException e) {
			log.error("Load IP data [17monipdb.dat] error!", e);
		}
	}

	public static void load(File resourceFile) {
		ipFile = resourceFile;
		load();
		if (enableFileWatch) {
			watch();
		}
	}

	public static void load(String filename, boolean strict) throws Exception {
		ipFile = new File(filename);
		if (strict) {
			int contentLength = Long.valueOf(ipFile.length()).intValue();
			if (contentLength < 512 * 1024) {
				throw new Exception("ip data file error.");
			}
		}
		load();
		if (enableFileWatch) {
			watch();
		}
	}

	public static String[] find(String ip) {
		int ip_prefix_value = new Integer(ip.substring(0, ip.indexOf(".")));
		long ip2long_value = ip2long(ip);
		int start = index[ip_prefix_value];
		int max_comp_len = offset - 1028;
		long index_offset = -1;
		int index_length = -1;
		byte b = 0;
		for (start = start * 8 + 1024; start < max_comp_len; start += 8) {
			if (int2long(indexBuffer.getInt(start)) >= ip2long_value) {
				index_offset = bytesToLong(b, indexBuffer.get(start + 6), indexBuffer.get(start + 5), indexBuffer.get(start + 4));
				index_length = 0xFF & indexBuffer.get(start + 7);
				break;
			}
		}

		byte[] areaBytes;

		lock.lock();
		try {
			dataBuffer.position(offset + (int) index_offset - 1024);
			areaBytes = new byte[index_length];
			dataBuffer.get(areaBytes, 0, index_length);
		} finally {
			lock.unlock();
		}

		return new String(areaBytes, Charset.forName("UTF-8")).split("\t", -1);
	}

	private static void watch() {
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable(){
			@Override
			public void run() {
				long time = ipFile.lastModified();
				if (time > lastModifyTime) {
					lastModifyTime = time;
					load();
				}
			}
		}, 1000L, 5000L, TimeUnit.MILLISECONDS);
	}

	private static void load() {
		lastModifyTime = ipFile.lastModified();
		FileInputStream fin = null;
		lock.lock();
		try {
			dataBuffer = ByteBuffer.allocate(Long.valueOf(ipFile.length()).intValue());
			fin = new FileInputStream(ipFile);
			int readBytesLength;
			byte[] chunk = new byte[4096];
			while (fin.available() > 0) {
				readBytesLength = fin.read(chunk);
				dataBuffer.put(chunk, 0, readBytesLength);
			}
			dataBuffer.position(0);
			int indexLength = dataBuffer.getInt();
			byte[] indexBytes = new byte[indexLength];
			dataBuffer.get(indexBytes, 0, indexLength - 4);
			indexBuffer = ByteBuffer.wrap(indexBytes);
			indexBuffer.order(ByteOrder.LITTLE_ENDIAN);
			offset = indexLength;

			int loop = 0;
			while (loop++ < 256) {
				index[loop - 1] = indexBuffer.getInt();
			}
			indexBuffer.order(ByteOrder.BIG_ENDIAN);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (fin != null) {
					fin.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			lock.unlock();
		}
	}

	private static long bytesToLong(byte a, byte b, byte c, byte d) {
		return int2long((((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff)));
	}

	private static int str2Ip(String ip) {
		String[] ss = ip.split("\\.");
		int a, b, c, d;
		a = Integer.parseInt(ss[0]);
		b = Integer.parseInt(ss[1]);
		c = Integer.parseInt(ss[2]);
		d = Integer.parseInt(ss[3]);
		return (a << 24) | (b << 16) | (c << 8) | d;
	}

	private static long ip2long(String ip) {
		return int2long(str2Ip(ip));
	}

	private static long int2long(int i) {
		long l = i & 0x7fffffffL;
		if (i < 0) {
			l |= 0x080000000L;
		}
		return l;
	}
	
	public static String randomIp() {
		Random r = new Random();
		StringBuffer str = new StringBuffer();
		str.append(r.nextInt(1000000) % 255);
		str.append(".");
		str.append(r.nextInt(1000000) % 255);
		str.append(".");
		str.append(r.nextInt(1000000) % 255);
		str.append(".");
		str.append(0);

		return str.toString();
	}
	
	
	/**
	 * 获取客户端IP<br>
	 * 默认检测的Header：<br>
	 * 1、X-Forwarded-For<br>
	 * 2、X-Real-IP<br>
	 * 3、Proxy-Client-IP<br>
	 * 4、WL-Proxy-Client-IP<br>
	 * otherHeaderNames参数用于自定义检测的Header
	 * 
	 * @param request 请求对象
	 * @param otherHeaderNames 其他自定义头文件
	 * @return IP地址
	 */
	public static String getClientIP(javax.servlet.http.HttpServletRequest request) {
		String[] headers = new String[]{"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
		String ip;
		for (String header : headers) {
			ip = request.getHeader(header);
			if(false == isUnknow(ip)){
				return getMultistageReverseProxyIp(ip);
			}
		}
		
		ip = request.getRemoteAddr();
		return getMultistageReverseProxyIp(ip);
	}
	
	/**
	 * 检测给定字符串是否为未知，多用于检测HTTP请求相关<br/>
	 * 
	 * @param checkString 被检测的字符串
	 * @return 是否未知
	 */
	public static boolean isUnknow(String checkString) {
		return StringUtils.isBlank(checkString) || "unknown".equalsIgnoreCase(checkString);
	}
	
	/**
	 * 从多级反向代理中获得第一个非unknown IP地址
	 * @param ip 获得的IP地址
	 * @return 第一个非unknown IP地址
	 */
	public static String getMultistageReverseProxyIp(String ip){
		// 多级反向代理检测
		if (ip != null && ip.indexOf(",") > 0) {
			final String[] ips = ip.trim().split(",");
			for (String subIp : ips) {
				if(false == isUnknow(subIp)){
					ip = subIp;
					break; 
				}
			}
		}
		return ip;
	}
}
