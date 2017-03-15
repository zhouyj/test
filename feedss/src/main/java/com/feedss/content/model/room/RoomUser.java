package com.feedss.content.model.room;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.feedss.user.model.UserProfileVo;

/**
 * 房间用户信息<br>
 * @author wangjingqing
 * @date 2016-08-05
 */
public class RoomUser implements Serializable{

	/**
	 */
	private static final long serialVersionUID = 6414754233591303430L;

	private String streamId;//直播uuid
	
	private String uuid;//用户uuid
	
	private String nickName;//昵称
	
	private String avatar;//用户头像
	
	private Date createTime;//进来时间
	
	private Date updateTiem;//更新时间
	
	private int status;//状态  1：请求 2：连接中 3：已结束

	private String groupId;
	
	private int isFollow;//是否和主播关注
	
	private int isVip;//是否vip 0:否 1：是
	
	private List<HashMap<String,String>> roles;
	
	private UserProfileVo profile;
	
	public String getStreamId() {
		return streamId;
	}

	public void setStreamId(String streamId) {
		this.streamId = streamId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTiem() {
		return updateTiem;
	}

	public void setUpdateTiem(Date updateTiem) {
		this.updateTiem = updateTiem;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	
	public int getIsFollow() {
		return isFollow;
	}

	public void setIsFollow(int isFollow) {
		this.isFollow = isFollow;
	}

	
	public int getIsVip() {
		return isVip;
	}

	public void setIsVip(int isVip) {
		this.isVip = isVip;
	}

	public List<HashMap<String,String>> getRoles() {
		return roles;
	}

	public void setRoles(List<HashMap<String,String>> roles) {
		this.roles = roles;
	}

	public UserProfileVo getProfile() {
		return profile;
	}

	public void setProfile(UserProfileVo profile) {
		this.profile = profile;
	}

	@Override
	public String toString() {
		return "RoomUser [streamId=" + streamId + ", uuid=" + uuid + ", nickName=" + nickName + ", avatar=" + avatar
				+ ", createTime=" + createTime + ", updateTiem=" + updateTiem + ", status=" + status + ", groupId="
				+ groupId + ", isFollow=" + isFollow + ", isVip=" + isVip + "]";
	}
}
