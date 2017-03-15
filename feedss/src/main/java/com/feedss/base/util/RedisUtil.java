package com.feedss.base.util;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {

	private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	public enum KeyType {
		TOKEN, Configure, Sms_Login, Sms_Mobile_Bind, MessageUserSig, Device, // 设备
		UserFollowing// 关注
		, UserFollowedBy// 被关注
		, TempUserList//
		, UserVo // 用户信息
		, CreateCategory, ShowCategory, BlackList // 黑名单
		, Sms_Mobile_Register // 注册短信验证码
		, HOST_LIST_FOLLOWBY //主持人排行榜，按被关注数
	}

	public String generateKey(KeyType keyType, String key) {
		key = key.replaceAll("\\-", "");
		return keyType.name() + key;
	}

	public void set(KeyType type, String key, String value) {
		String cacheKey = generateKey(type, key);
		stringRedisTemplate.opsForValue().set(cacheKey, value);
	}

	public void set(KeyType type, String key, String value, long expiredTime) {
		String cacheKey = generateKey(type, key);
		stringRedisTemplate.opsForValue().set(cacheKey, value, expiredTime, TimeUnit.SECONDS);
	}

	public String get(KeyType type, String key) {
		String cacheKey = generateKey(type, key);
		logger.info("cachekey = " + cacheKey);
		return stringRedisTemplate.opsForValue().get(cacheKey);
	}

	public void delete(KeyType type, String key) {
		stringRedisTemplate.delete(generateKey(type, key));
	}

	/**
	 * 设置超时时间
	 * 
	 * @param type
	 * @param key
	 * @param expiredTime
	 *            （秒）
	 * @return
	 */
	public boolean setTime(KeyType type, String key, long expiredTime) {
		String cacheKey = generateKey(type, key);
		return stringRedisTemplate.expire(cacheKey, expiredTime, TimeUnit.SECONDS);
	}

	/**
	 * 获取剩余时间(秒)
	 * 
	 * @param type
	 * @param key
	 * @param expiredTime
	 * @return
	 */
	public long getTime(KeyType type, String key) {
		return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
	}

	public boolean exists(KeyType type, String key) {
		String cacheKey = generateKey(type, key);
		return stringRedisTemplate.hasKey(cacheKey);
	}

	// set 集合操作
	public SetOperations<String, String> getSetOperations() {
		return stringRedisTemplate.opsForSet();
	}

	public void addToSet(KeyType type, String key, long expiredTime, String... values) {
		String cacheKey = generateKey(type, key);
		getSetOperations().add(cacheKey, values);
		setTime(type, key, expiredTime);
	}

	public void addToSet(KeyType type, String key, String... values) {
		String cacheKey = generateKey(type, key);
		getSetOperations().add(cacheKey, values);
	}

	public Set<String> membersToSet(KeyType type, String key) {
		String cacheKey = generateKey(type, key);
		return getSetOperations().members(cacheKey);
	}

	public long removeMemberToSet(KeyType type, String key, String value) {
		String cacheKey = generateKey(type, key);
		return getSetOperations().remove(cacheKey, value);
	}

	public boolean existMemberToSet(KeyType type, String key, Object o) {
		String cacheKey = generateKey(type, key);
		return getSetOperations().isMember(cacheKey, o);
	}

	public long getCountToSet(KeyType type, String key) {
		String cacheKey = generateKey(type, key);
		return getSetOperations().size(cacheKey);
	}

	public Set<String> intersectionToSet(KeyType type1, String key1, KeyType type2, String key2) {
		String cacheKey1 = generateKey(type1, key1);
		String cacheKey2 = generateKey(type2, key2);
		return getSetOperations().intersect(cacheKey1, cacheKey2);
	}

	public Set<String> differenceToSet(KeyType type1, String key1, KeyType type2, String key2) {
		String cacheKey1 = generateKey(type1, key1);
		String cacheKey2 = generateKey(type2, key2);
		return getSetOperations().difference(cacheKey1, cacheKey2);
	}

}
