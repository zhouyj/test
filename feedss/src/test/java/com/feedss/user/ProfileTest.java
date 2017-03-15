package com.feedss.user;

import com.feedss.FeedssApplication;
import com.feedss.user.entity.Account;
import com.feedss.user.entity.Profile;
import com.feedss.user.repository.AccountRepository;
import com.feedss.user.service.ProfileService;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by qin.qiang on 2016/8/2 0002.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@SpringApplicationConfiguration(FeedssApplication.class)
public class ProfileTest {

    @Autowired
    ProfileService profileService;
    @org.junit.Test
    public void test(){
        Profile profile = new Profile();
        profile.setAvatar("http://555555.jpg");
        profile.setUserId("123");
        profile.setNickname("秦强");
//        profileService.updateProfile2Connect(profile);
    }
}
