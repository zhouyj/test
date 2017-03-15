package com.feedss.content.model.room;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 直播间<br>
 * @author wangjingqing
 * @date 2016-07-29
 */
@Data
public class Room implements Serializable{

	/**
	 */
	private static final long serialVersionUID = 7887608431346599699L;

	private String id;//房间标示

	private String uuid;//当前直播uuid
	
	private String parentId;//父类直播uuid
	
	private RoomUser roomUser;//主播信息
	
	private List<RoomUser> childRoomUser;//观众信息
	
	private List<Room> childRoom;//当前链接视频
	
	private List<String>  banRoomUserList;//禁止用户列表
	
	private List<String>  notAllowUsers;//不允许访问用户列表
	
	private int praiseCount;//赞数
	
	private Integer onLineCount;//在线人数
	
	private Date createTime;//创建时间
	
	private Date updateTime;//更新时间
	
	private int status;//房间状态 0:创建 1：推流  2：结束 3：异常 

	private String groupId;//组id
	
	private String playUri;//RTMP播放URI
	
	private String publishUri;// RTMP推流URI
	
	private String bannerId;// 广告Id
	
	private String name;//直播间标题

}
