package com.feedss.portal.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.feedss.FeedssApplication;
import com.feedss.content.entity.Article;
import com.feedss.content.service.ArticleService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(FeedssApplication.class)
public class TerminalServiceTest {

	@Autowired
	InteractionService interactionService;
	
	@Test
	public void testHandle(){
		interactionService.handleOverdue();
	}
}
