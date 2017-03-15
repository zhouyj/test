package com.feedss.manage.model;

import lombok.Data;

@Data
public class StreamVo{

	private String uuid;
	private String title;//标题
	private String categoryId;//分类
	private String categoryName;//分类名称
	private String type; //类型
	private int status; //流状态
	private int isSecret;//是否私密直播
	private String clearSecret;
	private String started;//直播开始时间
	private String ended;//直播结束时间
	private String userId; // 主播uuid
	private String userNickname;//主播昵称
	private String publishUri;//推流地址
	private String playUri;//播放地址
	private String playbackUri; //回放地址
	private int playCount;//播放次数
	private int praiseCount;//点赞次数
	
}
