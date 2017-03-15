package com.feedss.manage.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 预告<br>
 * @author wangjingqing
 * @date 2016-07-20
 */
public class TrailerVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4086935099051488529L;
	public enum TrailerStatus{
		Available,//可用
		Unavailable//不可用
	}
	private String uuid;
	private String title;//标题
	private String streamId;//直播uuid
	private Date playTime;//播放时间
	private String picUrl;//图片路径
	private String userId;//用户UUID
	private String content;//内容简介
	private Date aheadTime;//预热时间 
	private boolean isSendMsg;//是否发送消息 true：发送 false：不发送
	private String nickName;//用户昵称 
	private String playTiemStr;
	private Integer trStatus;
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getStreamId() {
		return streamId;
	}
	public void setStreamId(String streamId) {
		this.streamId = streamId;
	}
	public Date getPlayTime() {
		return playTime;
	}
	public void setPlayTime(Date playTime) {
		this.playTime = playTime;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getAheadTime() {
		return aheadTime;
	}
	public void setAheadTime(Date aheadTime) {
		this.aheadTime = aheadTime;
	}
	public boolean isSendMsg() {
		return isSendMsg;
	}
	public void setSendMsg(boolean isSendMsg) {
		this.isSendMsg = isSendMsg;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getPlayTiemStr(){
		if(playTime == null){
			return "";
		}
		playTiemStr = com.feedss.base.util.DateUtil.dateToString(playTime, com.feedss.base.util.DateUtil.FORMAT_STANDERD_MINUTE);
		return playTiemStr;
	}
	public void setPlayTiemStr(String playTiemStr) {
		this.playTiemStr = playTiemStr;
	}
	public Integer getTrStatus(){
		if(playTime == null){
			return 0;
		}
		trStatus = playTime.compareTo(new Date()) >= 0 ? 1:2;
		return trStatus;
	}
	public void setTrStatus(Integer trStatus) {
		this.trStatus = trStatus;
	}
	
}
