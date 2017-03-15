package com.feedss.base.util;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import com.feedss.user.entity.User;

/**
 * 待确认是否有用
 */
public class WebContextSupport {
	public static final int COOKIE_EXPIRED_MINUTES = 60;

	public static final String USER_KEY = "_user";

	private static final ThreadLocal<HttpServletRequest> request = new ThreadLocal<HttpServletRequest>();
	private static final ThreadLocal<HttpServletResponse> response = new ThreadLocal<HttpServletResponse>();

	public enum Scope {
		REQUEST, SESSION, COOKIE
	}

	/**
	 * 绑定HTTP对象
	 *
	 * @param request
	 * @param response
	 */
	public static void bind(HttpServletRequest request, HttpServletResponse response) {
		WebContextSupport.request.set(request);
		WebContextSupport.response.set(response);
	}

	/**
	 * 解绑HTTP对象
	 */
	public static void unbind() {
		WebContextSupport.request.remove();
		WebContextSupport.response.remove();
	}

	/**
	 * 获取当前请求对象
	 *
	 * @return
	 */
	public static HttpServletRequest getRequest() {
		return request.get();
	}

	/**
	 * 获取当前响应对象
	 *
	 * @return
	 */
	public static HttpServletResponse getResponse() {
		return response.get();
	}

	/**
	 * 获取根路径
	 *
	 * @return
	 */
	public static String getRootURL() {
		HttpServletRequest httpServletRequest = getRequest();
		return httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() + ":"
				+ httpServletRequest.getServerPort() + httpServletRequest.getContextPath();
	}

	/**
	 * 获取会话数据
	 *
	 * @param key
	 * @param scope
	 *            主存储范围
	 * @param scopes
	 *            附加存储范围
	 */
	public static Object get(String key, Scope scope, Scope... scopes) {
		return doGet(getRequest(), key, scope, scopes);
	}

	/**
	 * 设置会话数据
	 *
	 * @param key
	 * @param value
	 * @param scope
	 *            主存储范围
	 * @param scopes
	 *            附加存储范围
	 */
	public static void put(String key, Object value, Scope scope, Scope... scopes) {
		doSet(getRequest(), getResponse(), key, value, scope, scopes);
	}

	/**
	 * 取消会话数据
	 *
	 * @param key
	 * @param scopes
	 */
	public static void remove(String key, Scope scope, Scope... scopes) {
		doRemove(getRequest(), getResponse(), key, scope, scopes);
	}

	/**
	 * 如果session缺失数据,尝试从cookie中加载
	 *
	 * @param key
	 */
	public static void loadInSessionFromCookie(String key) {
		Object value = get(key, Scope.SESSION);
		if (value == null) {
			value = WebContextSupport.get(key, Scope.COOKIE);
			if (value != null) {
				WebContextSupport.put(key, value, Scope.SESSION);
			}
		}
	}

	/**
	 * 获取当前用户
	 *
	 * @return
	 */
	public static User currentUser() {
		return (User) get(USER_KEY, Scope.SESSION);
	}

	/**
	 * 存储会话数据
	 *
	 * @param request
	 * @param response
	 * @param key
	 * @param value
	 * @param scope
	 *            主存储范围
	 * @param scopes
	 *            附加存储范围
	 */
	private static void doSet(HttpServletRequest request, HttpServletResponse response, String key, Object value,
			Scope scope, Scope... scopes) {
		if (value == null) {
			return;
		}
		EnumSet<Scope> set = EnumSet.of(scope, scopes);
		if (set.contains(Scope.REQUEST)) {
			request.setAttribute(key, value);
		}
		if (set.contains(Scope.SESSION)) {
			request.getSession().setAttribute(key, value);
		}
		if (set.contains(Scope.COOKIE)) {
			// 客户端加密
			String encrypted_value = toString(value, true);
			Cookie cookie = new Cookie(key, encrypted_value);
			cookie.setMaxAge((int) TimeUnit.MINUTES.toMillis(COOKIE_EXPIRED_MINUTES));
			cookie.setPath("/");
			response.addCookie(cookie);
		}
	}

	/**
	 * 存储会话数据
	 *
	 * @param request
	 * @param response
	 * @param key
	 * @param scope
	 *            主存储范围
	 * @param scopes
	 *            附加存储范围
	 */
	private static void doRemove(HttpServletRequest request, HttpServletResponse response, String key, Scope scope,
			Scope... scopes) {

		EnumSet<Scope> set = EnumSet.of(scope, scopes);
		if (set.contains(Scope.REQUEST)) {
			request.removeAttribute(key);
		}
		if (set.contains(Scope.SESSION)) {
			request.getSession().removeAttribute(key);
		}
		if (set.contains(Scope.COOKIE)) {
			// 客户端加密
			Cookie cookie = new Cookie(key, "");
			cookie.setMaxAge(0);
			cookie.setPath("/");
			response.addCookie(cookie);
		}
	}

	/**
	 * 获取会话数据
	 *
	 * @param request
	 * @param key
	 * @param scope
	 *            主存储范围
	 * @param scopes
	 *            附加存储范围
	 */
	private static Object doGet(HttpServletRequest request, String key, Scope scope, Scope... scopes) {
		EnumSet<Scope> set = EnumSet.of(scope, scopes);
		Object value = null;
		if (set.contains(Scope.REQUEST)) {
			value = request.getAttribute(key);
			if (set.contains(Scope.REQUEST) || value != null) {
				return value;
			}
		}
		if (set.contains(Scope.SESSION)) {
			HttpSession session = request.getSession();
			value = session.getAttribute(key);
			if (set.contains(Scope.SESSION) || value != null) {
				return value;
			}
		}
		if (set.contains(Scope.COOKIE)) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals(key) && StringUtils.isNotEmpty((cookie.getValue()))) {
						value = parseString(cookie.getValue(), true);
						break;
					}
				}
			}
			if (set.contains(Scope.COOKIE) || value != null) {
				return value;
			}
		}
		return value;
	}

	/**
	 * 序列化对象
	 *
	 * @param value
	 *
	 * @return
	 */
	protected static String toString(Object value, boolean encrypted) {
		boolean isStr = value instanceof String;
		String str = isStr ? (String) value : jsonParser.toJson(value);
		str = encrypted ? Encryptor.DEFAULT.encrypt(str) : str;
		return isStr ? str : "JSON[" + value.getClass().getName() + "]:" + str;
	}

	/**
	 * 反序列化对象
	 *
	 * @param str
	 * @param encrypted
	 *
	 * @return
	 */
	protected static Object parseString(String str, boolean encrypted) {
		boolean isStr = str.indexOf("JSON[") == -1;
		String value = str;
		String clazzName = null;
		if (!isStr) {
			int index = str.indexOf(":");
			clazzName = str.substring(5, index - 1);
			value = str.substring(index + 1);
		}
		if (encrypted) {
			value = Encryptor.DEFAULT.decrypt(value);
		}
		try {
			return isStr ? value : jsonParser.toBean(value, Class.forName(clazzName));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static JsonParser jsonParser = JsonParser.DEFAULT;

}
