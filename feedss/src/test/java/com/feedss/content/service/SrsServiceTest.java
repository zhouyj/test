package com.feedss.content.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.feedss.FeedssApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(FeedssApplication.class)
public class SrsServiceTest {

	@Autowired
	SrsService srsService;
	
	@Test
	public void testPauseTerminalPublish(){
		String appName = "live";
		String terminalName = "av0";
		srsService.pauseTerminalPublish(terminalName, appName);
	}
}
