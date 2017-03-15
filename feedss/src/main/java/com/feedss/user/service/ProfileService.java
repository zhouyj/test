package com.feedss.user.service;

import com.feedss.user.entity.Profile;
import com.feedss.user.entity.User;

/**
 * Created by qinqiang on 2016/8/1.
 */
public interface ProfileService {

	public Profile saveOrUpdateProfile(Profile profile);

	public Profile createProfile(User u);

	public int updateProfile2Connect(Profile profile,String userId);

	public Profile findByUserId(String userId);
}
