package com.feedss.app;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.feedss.FeedssApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FeedssApplication.class)
@WebAppConfiguration
public class ButtonRepositoryTest {
	@Autowired
	ButtonRepository buttonRepository;
	@Autowired
	ButtonService buttonService;

	@Test
	public void test() {
		Button button = new Button();
		button.setParentId(null);
		button.setName("众品榜");
		button.setIconUrl("https://zplive.com/uploads/icons/rankList.png");
		button.setType("html5");
		button.setViewUrl("https://zplive.com/h5/rankList");
		button.setPosition("centers");
		button.setSort(3);
		button.setParameter(null);
		buttonRepository.save(button);

		List<Button> buttons = buttonService.getAllButtonList();
		for (Button b : buttons) {
			System.out.println(b);
		}
	}

}
