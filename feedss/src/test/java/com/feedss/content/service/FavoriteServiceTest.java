package com.feedss.content.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONObject;
import com.feedss.FeedssApplication;
import com.feedss.content.entity.Product;
import com.feedss.content.model.FavoriteItem;
import com.feedss.content.service.FavoriteService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(FeedssApplication.class)
public class FavoriteServiceTest {
	@Autowired
	FavoriteService favoriteService;

	String userId = "a97d8a1d-800c-437a-ab3b-7a38b4add17b";
	String productId = "06abb423-aada-44a9-a027-3c60d35428c2";
	String productId2 = "0dd63797-c4d6-4169-8193-1417b5973ab0";
	String productId3 = "8364608b-4394-4f8d-8662-23e89923ce05";

	@Test
	public void testIsFav(){
		
	}
	
	
	@Test
	public void testGetFavoriteList() {
		List<FavoriteItem<Product>> result = favoriteService.getFavorites(userId, "1204232c-9199-4625-ac94-757d8791302a", 10);
		for(FavoriteItem<Product> item:result){
			System.out.println(JSONObject.toJSONString(item));
		}
	}

	@Test
	public void testAddFavorite() {
		favoriteService.addFavorite(userId, productId3);
		
	}
}
