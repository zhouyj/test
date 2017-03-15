package com.feedss.content.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.feedss.base.BaseEntity;

/**
 * 视频流
 * 
 * @author tangjun
 *
 */
@Entity
@Data
@EqualsAndHashCode(callSuper=true)
public class Stream extends BaseEntity {
	public enum StreamType {
		Normal, // 普通
		HD, // 高清
		VR// VR
	}

	public enum StreamStatus {
		Created, // 创建完成
		Publishing, // 推流中
		Ended// 结束
	}

	@Column(length = 36, nullable = false)
	private String appName;// 应用名，如：lvmengya

	@Column(length = 36)
	private String parentId;// 父StreamID，主流该字段为空，连线流该字段为主流的uuid

	@JsonIgnore
	@Column(length = 32)
	private String secret;// 加密流密钥，MD5
	
	@JsonIgnore
	@Column(length = 32)
	private String clearSecret;//明文流密码

	@Column(length = 36, nullable = false)
	private String userId;// 用户uuid
	
	@Transient
	private String userNickname;// 用户昵称

	@Column(length = 512)
	private String cover;// 封面图片URI

	@Column(length = 36)
	private String category;// 分类
	
	@Transient
	private String categoryName;// 分类名称

	private Date started;// 开始时间
	private Date ended;// 结束时间
	private long duration;// 时长

	private double longitude;// 经度
	private double latitude;// 纬度

	@Column(length = 128)
	private String position;// 位置名称

	// 地址相关字段
	@Column(length = 256)
	private String publishUri;// RTMP推流URI

	@Column(length = 256)
	private String playUri;// RTMP播放URI

	@Column(length = 256)
	private String hlsUri;// HLS URI

	@Column(length = 1000)
	private String playbackUri;// 回放URI

	// 视频相关字段
	@Column(length = 20)
	private String fmt;// 封包格式，如：mp4、flv

	@Column(length = 20)
	private String vcodec;// 视频编码，如：h264

	@Column(length = 20)
	private String acodec;// 音频编码，如：aac

	private int rate;// 每秒帧数，如：25

	@Column(length = 20)
	private String size;// 分辨率，width x height，如：1280x720

	private int bitrate;// 码流，单位kbps

	private int price;// 价格，付费看直播

	@Column(nullable=false,name="is_delete")
	private boolean deleted;//用于逻辑删除 false：可用  true：删除
	@Column
	private int praiseCount;//赞数
	@Column
	private int playCount;//播放次数
	@Column
	private int streamStatus;//流状态
	@Column
	private Date streamUpdateDate;//流更新时间
	
	@Column(length = 36, nullable = true)
	private String bannerId;//广告Banner UUID
}
