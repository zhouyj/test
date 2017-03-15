package com.feedss.contact.repository;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.feedss.FeedssApplication;
import com.feedss.base.util.conf.ConfigureRepository;
import com.feedss.user.entity.Configure;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FeedssApplication.class)
@WebAppConfiguration
public class ConfigureRepositoryTest {

	@Autowired
	ConfigureRepository configRepo;
	
	@Test
	public void testSave(){
		Configure c = new Configure();
		c.setName("test002");
		c.setValue("123456");
		Configure tmp = configRepo.save(c);
		Assert.assertEquals(0, tmp.getId());
		tmp = configRepo.findOne(tmp.getUuid());
		Assert.assertNotEquals(0, tmp.getId());
		System.out.println(tmp.getId());
	}
}
