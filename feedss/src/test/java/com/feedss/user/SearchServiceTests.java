package com.feedss.user;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.feedss.FeedssApplication;
import com.feedss.user.service.UserSearchService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(FeedssApplication.class)
public class SearchServiceTests {
	@Autowired
	UserSearchService searchService;

	@Test
	public void testSearch() {
		searchService.keywordSearch("绿", 0, 100);
	}

}
