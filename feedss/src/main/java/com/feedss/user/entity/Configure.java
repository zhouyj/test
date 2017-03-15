package com.feedss.user.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

/**
 * 配置
 * 
 * @author tangjun
 *
 */
@Entity
@Data
public class Configure{
	public enum ConfigType {
		ALL, APP, SERVER
	}
	
	public enum ConfigCategory {
		
	}
	
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition=" int(20) not null  AUTO_INCREMENT,key(id) ",unique=true)
	private long id;

	@Id
	@Column(length = 36, unique = true, nullable = false)
	private String uuid;

	@Column(length = 50, unique = true, nullable = false)
	private String name;

	@Column(columnDefinition = "text")
	private String value;
	
	private Integer type;
	
	private Integer category;
	
	private String description;

	public Configure(){
		this.uuid = UUID.randomUUID().toString();
	}
}
