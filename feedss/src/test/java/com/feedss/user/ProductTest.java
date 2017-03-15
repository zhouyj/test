package com.feedss.user;

import com.feedss.FeedssApplication;
import com.feedss.user.entity.Role;
import com.feedss.user.entity.UsrProduct;
import com.feedss.user.model.ProductVo;
import com.feedss.user.repository.RoleRepository;
import com.feedss.user.repository.UsrProductRepository;
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
public class ProductTest {
	@Autowired
	private UsrProductService productService;

	@org.junit.Test
	public void test(){
		List<ProductVo> products = productService.getProductsByType("TT");
		for(ProductVo product:products){
			System.out.println(product);
		}
	}
}
