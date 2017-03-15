package com.feedss.base.util.conf;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.feedss.base.Constants;
import com.feedss.user.entity.Configure;

@Component
@EnableScheduling // 启用定时任务
public class ConfigureUtil {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public final static long CONFIG_EXPIREDTIME = 60 * 60;

	@Autowired
	private ConfigureRepository configureRespository;

	@Autowired
	private StringRedisTemplate redisTemplate;

	public ConfigureUtil() {
		logger.info("ConfigureUtil...");
	}

	/**
	 *
	 * @param key
	 *            SITE_TITLE // 站点名称 SITE_LOGO_URL // 网站logo SITE_BANNER_URL //
	 *            企业宣传图 SITE_INTEGRATION_NAME // 积分名称 WEXIN_APPID 
	 *
	 * @return String
	 */
	public String getConfigureValue(String key) {
		String value = getConfig(key);
		if (null == value) {
			switch (key) {
			case Constants.SITE_TITLE:
				return "管理后台";
			case Constants.SITE_INTEGRATION_NAME:
				return "虚拟币";
			default:
				return "";
			}
		}
		return value;
	}

	@Scheduled(cron = "0 0/1 * * * ?") // 每1分钟执行一次
	public void scheduler() {
		logger.info(">>>>>>>>>>>>> load config ... " + new Date());
		Iterable<Configure> iterable = configureRespository.findAll();
		for (Configure conf : iterable) {
			redisTemplate.opsForValue().set(Constants.CacheKeyType.Configure.name() + conf.getName(), conf.getValue(),
					CONFIG_EXPIREDTIME, TimeUnit.SECONDS);
		}
	}

	public String getConfig(String name) {
		String value = redisTemplate.opsForValue().get(Constants.CacheKeyType.Configure.name() + name);
		if (StringUtils.isEmpty(value)) {
			Configure cf = configureRespository.findByName(name);
			if (cf == null)
				return null;
			else
				return cf.getValue();
		}
		return value;
	}
	
	public int getConfigIntValue(String name){
		String value = getConfig(name);
		if(StringUtils.isEmpty(value)) return 0;
		try{
			return Integer.parseInt(value);
		}catch (Exception e) {
			return 0;
		}
	}
	
	public Iterable<Configure> findByType(Integer type){
		return configureRespository.findByType(type);
	}
	
	public Iterable<Configure> findByCategory(Integer category){
		return configureRespository.findByCategory(category);
	}

	public Configure save(String name, String value) {
		Configure conf = new Configure();
		conf.setName(name);
		conf.setValue(value);
		conf = configureRespository.save(conf);
		redisTemplate.opsForValue().set(Constants.CacheKeyType.Configure.name() + conf.getName(), conf.getValue(),
				CONFIG_EXPIREDTIME, TimeUnit.SECONDS);
		return conf;
	}

	public boolean save(String siteTitle, String siteLogoUrl, String siteBannerUrl, String siteIntegrationName,
			String weChatAppId, String weChatAppSecret, String appLeftModelName, String appLeftModelUrl,
			String appRightModelName, String appRightModelUrl) {
		if (!StringUtils.isEmpty(siteTitle)) {
			save(Constants.SITE_TITLE, siteTitle);
		}

		if (!StringUtils.isEmpty(siteLogoUrl)) {
			save(Constants.SITE_LOGO_URL, siteLogoUrl);
		}

		if (!StringUtils.isEmpty(siteBannerUrl)) {
			save(Constants.SITE_BANNER_URL, siteBannerUrl);
		}

		if (!StringUtils.isEmpty(siteIntegrationName)) {
			save(Constants.SITE_INTEGRATION_NAME, siteIntegrationName);
		}

		if (!StringUtils.isEmpty(appLeftModelName)) {
			save(Constants.APP_LEFTMODEL_NAME, appLeftModelName);
		}
		if (!StringUtils.isEmpty(appLeftModelUrl)) {
			save(Constants.APP_LEFTMODEL_URL, appLeftModelUrl);
		}
		if (!StringUtils.isEmpty(appRightModelName)) {
			save(Constants.APP_RIGHTMODEL_NAME, appRightModelName);
		}
		if (!StringUtils.isEmpty(appRightModelUrl)) {
			save(Constants.APP_RIGHTMODEL_URL, appRightModelUrl);
		}
		return true;
	}
}
