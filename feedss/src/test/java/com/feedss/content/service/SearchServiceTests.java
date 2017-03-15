package com.feedss.content.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.feedss.FeedssApplication;
import com.feedss.content.service.ContentSearchService;
import com.feedss.user.service.UserSearchService;

import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@SpringApplicationConfiguration(FeedssApplication.class)
public class SearchServiceTests extends TestCase  {
	@Autowired
	ContentSearchService searchService;
	
	@Autowired
	UserSearchService userSearchService;

	@Test
	public void testSearch() {
		searchService.keywordSearch("测试", 0, 100);
	}

	@Test
	public void testGeoSearch() {
		searchService.geoSearch(39.915, 116.295, 0, 50);
	}
	
	public void testSearchUser(){
		userSearchService.keywordSearch("VR云女眼", 0, 100);
	}
}
