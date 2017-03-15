package com.feedss.content.service;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONObject;
import com.feedss.FeedssApplication;
import com.feedss.base.Pages;
import com.feedss.content.entity.Stream;
import com.feedss.content.repository.StreamRepository;
import com.feedss.content.service.StreamService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(FeedssApplication.class)
public class StreamServiceTests {
	@Autowired
	StreamService streamService;
	
	@Autowired
	StreamRepository streamRep;
	
	@Test
	public void testGetTotalStreamTime(){
		String userId = "6253d1b3-2863-4ecd-980a-d7f7a8eeecee";
		System.out.println(streamService.getTotalStreamTime(userId));
	}

	@Test
	public void testGetStreamList() {
		int status = 0;
		String userId = null;
		String categoryId = null;
		String title = null;
		int pageNo = 1;
		int pageSize = 20;
		
		Pages<Stream> result = streamService.findStreams(status, userId, categoryId, title, pageNo, pageSize);
		System.out.println("===============================total count: " + result.getTotalCount());
		for(Stream s:result.getList()){
			System.out.println(JSONObject.toJSON(s));
		}
	}
	
	@Test
	public void testIsNull(){
		String streamId = "b0d22ed8-90a8-41f7-a23b-72d183b92259";
		streamId = "8ba1ef13-67e6-4440-8474-7e0701a6b739";
		Stream stream = streamService.selectStream(streamId);
		if(StringUtils.isNotEmpty(stream.getPlaybackUri())){
			System.out.println("--------------is not empty");
		}else{
			System.out.println("--------------is empty");
		}
	}
}
