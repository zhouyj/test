package com.feedss.user.model;

import java.util.Date;

import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.feedss.user.entity.Favorite;

/**
 * 收藏
 * @author 张杰
 *
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class FavoriteVo {

	String uuid;
	
	String object;
	
	String objectId;
	
	Date created;
	
	String name;
	
	String extAttr;
	
	String type;
	
	public FavoriteVo(Favorite f){
		this.created=f.getCreated();
		this.object=f.getObject();
		this.objectId=f.getObjectId();
		this.uuid=f.getUuid();
		this.name = f.getName();
		this.type = f.getType();
		this.extAttr = f.getExtAttr();
	}
	
	
}
