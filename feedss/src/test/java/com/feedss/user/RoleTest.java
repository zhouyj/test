package com.feedss.user;

import com.feedss.FeedssApplication;
import com.feedss.user.entity.Role;
import com.feedss.user.entity.User;
import com.feedss.user.repository.RoleRepository;
import com.feedss.user.repository.UserRepository;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by qinqiang on 2016/7/30.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@SpringApplicationConfiguration(FeedssApplication.class)
public class RoleTest {
	@Autowired
	private RoleRepository roleRepository;

	@org.junit.Test
	public void test(){
		List<Role> roles = roleRepository.findRolesByUserId("111");
		for(Role role:roles){
			System.out.println(role.getName()+"---"+role.getCode());
		}
	}
}
