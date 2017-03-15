package com.feedss.contact.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "user_signature")
public class AccountUsersig {
	
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Id
	@Column(length = 36, unique = true, nullable = false)
	private String uuid;
	
	@Column(length = 36, unique = true, nullable = false)
	private String accountId;
	
	@Column(columnDefinition = "text")
	private String userSig;
	

	public AccountUsersig(){
		this.uuid = UUID.randomUUID().toString();
	}
	
	public AccountUsersig(String accountId, String userSig){
		this.uuid = UUID.randomUUID().toString();
		this.accountId = accountId;
		this.userSig = userSig;
	}
}
