package com.feedss.user;

import com.feedss.FeedssApplication;
import com.feedss.user.entity.Account;
import com.feedss.user.repository.AccountRepository;
import com.feedss.user.repository.AccountTransactionRepository;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by qin.qiang on 2016/8/2 0002.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@SpringApplicationConfiguration(FeedssApplication.class)
public class AccountTest {

    @Autowired
    AccountRepository accountRepository;
    
    @Autowired
    AccountTransactionRepository  accountTransactionRepository;
    
    @org.junit.Test
    public void test(){
      Account account =  accountRepository.findByUserId("9");
        System.out.println(account==null);
    }
    
    @org.junit.Test
    public void getBalance(){
      int value =  accountTransactionRepository.getBalance("bd96c84d-09ce-4247-bf35-b189eef9dec5");
        System.out.println(value);
    }
    
}
