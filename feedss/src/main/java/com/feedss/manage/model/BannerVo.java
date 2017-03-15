package com.feedss.manage.model;


import java.util.Date;

import lombok.Data;

/**
 * @author wangjingqing
 * @date 2016-07-20
 */
@Data
public class BannerVo{

	private String uuid;
	private int type;
	private String content;//内容

	private String picUrl;//图片路径
	private String title;//标题
	private Date playStart;//直播开始时间

	private Date playEnd;//直播结束时间
	private Date start;//开始时间
	private Date end;//结束时间

	private String location;//结束时间
	private int sort;//排序
	
}
