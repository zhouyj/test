package com.feedss.content.controller.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;

/**
 * 
 * @author tangjun
 *
 */
@Entity
public @Data class CallbackEvent {
	// 通用字段
	@GeneratedValue
	private long id;

	@Id
	@Column(length = 36, nullable = false)
	private String uuid;
	private Date created;

	// 回调通知接口字段，流唯一标识：rtmp://{vhost}/{app}/{stream}
	private String action;// 事件的动作，见ActionType枚举，
	private long client_id;// 客户端的ID，如：32930
	private String ip;// 客户端IP地址，如：180.76.186.156
	private String vhost;// 虚拟主机，如：live.feedss.com
	private String app;// 应用名称，如：lvshang
	private String stream;// 流名称，如：livestream
	private String tcUrl;// 如：rtmp://live.feedss.com/live/
	private String pageUrl;// 页面URL：如：http://live.feedss.com/livestream.html
	private String cwd;// 目录，如：/usr/local/srs
	private String file;// 录制文件名，如：/home/java/soft/nginx/html/live.feedss.com/live/livestream/2016/08/14/23.16.54.959.flv
	private long send_bytes;// 发送字节数
	private long recv_bytes;// 接收字节数

	public enum ActionType {
		on_connect, // 连接建立
		on_close, // 连接关闭
		on_publish, // 开始推流
		on_unpublish, // 结束推流
		on_play, // 开始播放
		on_stop, // 停止播放
		on_dvr // 录制完成
	}

	public CallbackEvent() {
		this.uuid = UUID.randomUUID().toString();
		this.created = new Date();
	}

}
