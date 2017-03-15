package com.feedss.base;
/**
 * 错误枚举
 * @author zhouyujuan
 *
 */
public enum ErrorCode {
	SUCCESS(0, "SUCCESS"),
	FAIL(-1, "FAILURE"),

	INVALID_PARAMETERS(10001, "参数错误"),
	INTERNAL_ERROR(10002, "系统内部错误"),

	NO_AUTH(10003, "无权限"),
	INVALID_TOKEN(10004, "账号异常，请重新登录"),
	ERROR(10005, "处理失败"),
	NO_CHANNEL(10006, "没有找到该渠道版本"),
	
	INVALIDE_CODE(10110, "验证码错误"),
	USER_NOT_EXIST(10111, "用户不存在"),
	USER_FORBIDDEN(10112, "用户被禁用"),
	PASSWORD_ERROR(10113, "密码信息错误"),
	MOBILE_UNREGISTER(10114, "手机号尚未注册"),
	USERNAME_OR_PASSWORD_ERROR(10115, "用户名／密码错误"),

	NEED_USERNAME_AND_PASSWORD(10116, "请输入用户名和密码"),
	ACCOUNT_EXISTED(10117, "账号已存在"),
	
	CARD_OR_PASSROD_ERROR(10118, "学习卡／密码 错误"),
	CARD_ACTIVED(10119, "学习卡已激活"),
	INVALIDE_CARD(10120, "学习卡无效"),
	HAS_BIND_CARD(10121, "已经绑定学习卡"),
	
	MOBILE_HAS_BINDED(10130, "手机号已绑定其他账号"),
	CODE_ERROR(10131, "验证码错误"),
	HAS_BINDED_MOBILE(10132, "该账号已绑定其他手机号"),

	//角色
	ROLE_NOT_EXIST(10160, "角色不存在"),
	//关系
	HAS_FOLLOW(10210, "已经关注"),
	CANT_FOLLOW_SELF(10211, "自己不能关注自己"),

	NOT_ENOGH_BANLANCE(10340, "余额不足,请充值!"),
	//点赞
	HAS_LIKED(10410, "已点赞"),
	//收藏
	HAS_FAVORITED(10430, "已收藏"),
	OBJECT_NOT_EXIST(10431, "删除失败,对象不存在"),

	WEIXIN_ORDER_FAILURE(10510, "微信下单失败!"),
	FIND_ORDER_FAILURE(10520, "查询订单失败!"),

	UPLOAD_ERROR(10610, "上传失败"),
	UPLOADFILE_EMPTY(10611, "上传文件为空"),
	SEND_SMS_FAILED(10620, "发送短信失败"),

	INTERNAL_FAILURE(11000, "操作失败，稍后重试"),
	VIDEO_NOT_EXISTED(11001,"视频不存在"),
	CHECK_PASSWORD_ERROR(11002,"密码不正确"),
	HOST_ERROR(11003,"该视频不属于当前用户"),
	IS_LIVING(11004,"该视频正在直播中"),
	NOT_ADVERT(11005,"用户当前没有预约"),
	HAS_ADVERT(11006,"您已预约"),
	LIVE_IS_OVER(11007,"直播已结束"),
	TOO_MANY_AUDIENCE(11008,"最多只可接通2位观众"),
	CHOOSE_ONE_AUDIENCE(11009,"请选择一位观众"),
	HAS_GET_OUT(11010,"您已被主播踢出房间"),
	PULL_PRODUCT_AUTHFAILURE(11011,"拉取商品认证失败"),
	CHOOSE_ONE_PRODUCT(11012,"请选择一款商品"),
	NO_LIVE_NEED_CLOSE(11013,"当前没有要关闭的视频"),
	PRODUCT_NOT_EXIST(11014,"商品不存在"),
	PRODUCT_PRICE_ERROR(11015,"商品价格不符合"),
	ADVERT_NOT_EXIST(11016,"预约信息不存在"),
	TERMINAL_NOT_EXIST(11017, "设备不存在"),
	TERMINAL_NEED_PUBLISHURL(11018, "设备缺少推流地址"),
	TERMINAL_PUBLISHING(11019, "设备推流中"),
	TRAILER_RELATED_ERROR(11020, "设备推流中"),
	
    INVALIDPARAMETERS(12001, "缺少必填参数"),
    INTERNALERROR(12003, "内部错误"),
	GET_OUT(12004, "被主播踢出聊天室了"),
	SHUT_UP(12005, "您已被主播禁言"),
	GROUP_NOT_EXIST(12006, "群不存在"),
	SYS_MESSAGE_SENDED(12007, "该系统消息已被发送"),
	
	BALANCE_NOT_ENOUGH(13006, "余额不足"),
	HAVE_HANDLED(13007, "已处理"),

	TITLE_IS_EMPTY(20001, "标题不能为空"),
	CHOOSE_USER(20002, "请选择用户"),
	UPLOAD_IMAGE(20003, "请上传图片"),
	CHOOSE_TIME(20004, "请选择预告时间"),
	DESCRIPTION_TOO_LONG(20005, "简介内容过长"),
	BANNER_TOO_MANY(20006, "广告数量已达上限，请先删除部分数据"),
	REQUIRED_INFO(20007, "请完善必填信息!"),
	
	
	
	;
	
    
    private String msg;
	
	private int code;
	
	private ErrorCode( int code, String msg){
		this.code = code;
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
	
}
