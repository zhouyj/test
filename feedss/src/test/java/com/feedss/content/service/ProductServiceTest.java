package com.feedss.content.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.feedss.FeedssApplication;
import com.feedss.content.entity.Product;
import com.feedss.content.service.ProductService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(FeedssApplication.class)
public class ProductServiceTest {
	
	@Autowired
	private ProductService productService;
	
	@Test
	public void testPullProduct(){
		String userId = "3090ed81-e724-46f9-afa4-301adf193f0c";
		int pageNo = 1;
		int pageCount= 20;
		String token = "7c3a961afdd86fc7de9b49ee5ef03d94e82fe9fb";
		
		List<Product> list = productService.pullProduct(userId, pageNo, pageCount, token);
		
		
		Assert.assertNotNull(list);
	}

}
