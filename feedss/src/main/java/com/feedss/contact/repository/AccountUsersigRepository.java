package com.feedss.contact.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.feedss.contact.entity.AccountUsersig;

public interface AccountUsersigRepository extends CrudRepository<AccountUsersig, String>{
	
	@Query("select au from AccountUsersig au where au.accountId= :accountId")
	public AccountUsersig findByAccountId(@Param("accountId")String accountId);
}
