package com.feedss.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.feedss.base.BaseEntity;

import lombok.Data;

/**
 * 用户关系
 * 
 * @author tangjun
 *
 */
@Entity
@Data
public class UserRelation extends BaseEntity {
	public enum RelationType {
		FOLLOW, // 关注关系
		FRIEND,// 朋友关系
		BLACK //拉黑关系
	}
	
	public enum FollowType{
		UNFOLLOW,  //取消关注
		FOLLOW    //关注
	}

	@Column(length = 36, nullable = false)
	private String fromUserId;// 源用户Id

	@Column(length = 36, nullable = false)
	private String toUserId;// 目的用户Id
	
	@OneToOne
	@JoinColumn(name = "fromId", foreignKey = @ForeignKey(name = "none"))
	User fromUser;
	
	@OneToOne
	@JoinColumn(name = "toId", foreignKey = @ForeignKey(name = "none"))
	User toUser;

}
