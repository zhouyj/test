package com.feedss.base;

public class Constants {

	public enum CacheKeyType {
		Configure, // 配置项
		Group,// 群
	}

	// user status
	public final static int USER_STATUS_DEL = -1;
	public final static int USER_STATUS_BLOCKED = 0;
	public final static int USER_STATUS_NORMAL = 1;

	public static final String PASSPORT_USERID = "userId";

	/**
	 * app两侧模块名称及链接配置key
	 */
    public static final String APP_LEFTMODEL_NAME = "app_leftmodel_name";
    public static final String APP_LEFTMODEL_URL = "app_leftmodel_url";
    public static final String APP_RIGHTMODEL_NAME = "app_rightmodel_name";
    public static final String APP_RIGHTMODEL_URL = "app_rightmodel_url";
    
	public static final String SITE_TITLE = "site_title";
	public static final String SITE_LOGO_URL = "site_logo_url";
	public static final String SITE_BANNER_URL = "site_banner_url";
	public static final String SITE_INTEGRATION_NAME = "site_integration_name";
	
	public static final String APP_ZHIBO_FILTER_HOST = "app_zhibo_filter_host";
	public static final String APP_ZHIBO_FILTER_AUDIENCE = "app_zhibo_filter_audience";
	
	public static final String APP_ZHIBO_SWITCH_CAMERA_HOST = "app_zhibo_switch_camera_host";
	public static final String APP_ZHIBO_SWITCH_CAMERA_AUDIENCE = "app_zhibo_switch_camera_audience";

	public static final String APP_ZHIBO_SHARE_HOST = "app_zhibo_share_host";
	public static final String APP_ZHIBO_SHARE_AUDIENCE = "app_zhibo_share_audience";
	
	public static final String APP_ZHIBO_IM_HOST = "app_zhibo_im_host";
	public static final String APP_ZHIBO_IM_AUDIENCE = "app_zhibo_im_audience";
	
	public static final String APP_ZHIBO_CLEAN_SCREEN_HOST = "app_zhibo_clean_screen_host";
	public static final String APP_ZHIBO_CLEAN_SCREEN_AUDIENCE = "app_zhibo_clean_screen_audience";

	public static final String APP_ZHIBO_RELEASE_TASK_HOST = "app_zhibo_release_task_host";
	public static final String APP_ZHIBO_RELEASE_TASK_AUDIENCE = "app_zhibo_release_task_audience";

	public static final String APP_ZHIBO_RELEASE_BONUSTASK_HOST = "app_zhibo_release_bonustask_host";
	public static final String APP_ZHIBO_RELEASE_BONUSTASK_AUDIENCE = "app_zhibo_release_bonustask_audience";

	public static final String APP_ZHIBO_RELEASED_TASKS_HOST = "app_zhibo_released_tasks_host";
	public static final String APP_ZHIBO_RELEASED_TASKS_AUDIENCE = "app_zhibo_released_tasks_audience";

	public static final String APP_ZHIBO_RELEASED_BONUSTASKS_HOST = "app_zhibo_released_bonustasks_host";
	public static final String APP_ZHIBO_RELEASED_BONUSTASKS_AUDIENCE = "app_zhibo_released_bonustasks_audience";
	
	public static final String APP_ZHIBO_RELEASE_BID_HOST = "app_zhibo_release_bid_host";
	public static final String APP_ZHIBO_RELEASE_BID_AUDIENCE = "app_zhibo_release_bid_audience";
	
	public static final String APP_ZHIBO_RELEASED_BIDS_HOST = "app_zhibo_released_bids_host";
	public static final String APP_ZHIBO_RELEASED_BIDS_AUDIENCE = "app_zhibo_released_bids_audience";
	
	public static final String APP_ZHIBO_DO_LIKE_HOST = "app_zhibo_do_like_host";
	public static final String APP_ZHIBO_DO_LIKE_AUDIENCE = "app_zhibo_do_like_audience";
	
	public static final String APP_ZHIBO_H5_STORE_HOST = "app_zhibo_h5_store_host";
	public static final String APP_ZHIBO_H5_STORE_AUDIENCE = "app_zhibo_h5_store_audience";
	
	public static final String APP_ZHIBO_H5_HOME_HOST = "app_zhibo_h5_home_host";
	public static final String APP_ZHIBO_H5_HOME_AUDIENCE = "app_zhibo_h5_home_audience";
	
	public static final String APP_ZHIBO_H5_HOUSE_HOST = "app_zhibo_h5_house_host";
	public static final String APP_ZHIBO_H5_HOUSE_AUDIENCE = "app_zhibo_h5_house_audience";
	
	public static final String PUBLISH_DOMAIN = "publishDomain";
	public static final String PLAY_DOMAIN = "playDomain";
	public static final String PLAYBACK_DOMAIN = "playbackDomain";
	public static final String HLS_DOMAIN = "hlsDomain";
	public static final String SRS_API = "srsApi";
	public static final String APP_NAME = "appName";
	public static final String H5_FOOTER = "h5.footer.app";
	public static final String ROOM_OUT_TIME = "roomOutTime";
	public static final String SMS_HOST = "Sms.host";
	public static final String SMS_APPID = "Sms.appid";
	public static final String SMS_APPTOKEN = "Sms.apptoken";
	public static final String PROJECT_VERSION = "Project.Version";
	public static final String DOWNLOAD_URL = "download.url";
	public static final String IOS_URL = "ios.url";
	public static final String ANDROID_URL = "android.url";
	public static final String AGREEMENT_URL = "agreement.url";
	public static final String AGREEMENT_FILE = "User.Agreement.HtmlPath";
	public static final String CLOUD_URL = "cloud.url";
	public static final String MALL_DOMAIN = "mallDomain";
	public static final String TEMP_DIR = "temp.dir";
	public static final String IMAGE_HOST = "Image.host";
	public static final String IMAGE_PATH = "Image.path";
	public static final String REGISTER_CONTENT = "REGISTER_CONTENT";
	public static final String LOGIN_CONTENT = "LOGIN_CONTENT";
	public static final String MOBILE_BIND_CONTENT = "MOBILE_BIND_CONTENT";
	public static final String ACCOUNT_BUYLIST = "Account.buylist";
	public static final String RECHARGE_RATIO = "Recharge.ratio";
	public static final String WECHAT_APPNAME = "WeChat.appname";
	public static final String WECHAT_APPID = "WeChat.AppId";
	public static final String WECHAT_KEY = "WeChat.key";
	public static final String WECHAT_MERCHANTID = "WeChat.merchantId";
	public static final String WECHAT_NOTIFY_URL = "WeChat.notifyUrl";
	public static final String WECHAT_UNIFIED_ORDER = "WeChat.unifiedOrder";
	public static final String WECHAT_ORDER_QUERY = "WeChat.orderQuery";
	public static final String ACCOUNT_TRANSACTION_HASEXPIRED = "Account.transaction.hasExpired";
	public static final String ACCOUNT_TRANSACTION_SYSTEMPRESEND_EXPIRED = "Account.transaction.systemPresent.expired";
	public static final String REGISTER_NOTEMPTY_FIELD = "Register.notempty.field";
	public static final String REGISTER_REWARD_COUNT = "Register.reward.count";
	public static final String PROFILE_REWARD_FIELD = "Profile.reward.field";
	public static final String PROFILE_REWARD_COUNT = "Profile.reward.count";
	public static final String REWARD_ARTICLEDETAIL_COUNT = "Reward.articleDetail.count";
	public static final String REWARD_LOGIN_COUNT = "Reward.login.count";
	
}
