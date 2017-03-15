package com.feedss.user;

import com.feedss.FeedssApplication;
import com.feedss.user.model.ProductVo;
import com.feedss.user.service.UserProductService;
import com.feedss.user.service.UsrProductService;

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
public class UserProductTest {
	@Autowired
	private UserProductService userProductService;

	@org.junit.Test
	public void test(){
			System.out.println(userProductService.getProductsByUserId("6"));
	}
}
