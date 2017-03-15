package com.feedss.content.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.feedss.FeedssApplication;
import com.feedss.content.entity.Article;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(FeedssApplication.class)
public class ArticleServiceTest {

	@Autowired
	ArticleService articleService;
	
	@Test
	public void testSelectArticlePages(){
		String categoryId = "b44214f7-8a11-45a0-b8c5-b2199cdc042b";
		Integer status = 2;
		Integer pageNo = 0;
		Integer pageSize = 10;
		
		Page<Article> pages = articleService.selectArticlePages(categoryId, status, pageNo, pageSize);
		
		System.out.println(pages.hasContent());
	}
}
