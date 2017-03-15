package com.feedss.content.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class FavoriteItem<T> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4612018477029021341L;
	private String uuid;
	private String object;
	private String objectId;
	private long created;
	private T t;
	
}
