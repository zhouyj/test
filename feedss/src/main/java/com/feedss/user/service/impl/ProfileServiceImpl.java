package com.feedss.user.service.impl;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feedss.base.Constants;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.contact.service.QCloudService;
import com.feedss.user.entity.Profile;
import com.feedss.user.entity.User;
import com.feedss.user.repository.ProfileRepository;
import com.feedss.user.service.ProfileService;


/**
 * Created by qinqiang on 2016/8/1.
 */
@Service
public class ProfileServiceImpl implements ProfileService{

	@Autowired
	private ProfileRepository profileRepository;
	@Autowired
	QCloudService qCloudService;
	@Autowired
	ConfigureUtil configureUtil;
    
	@Override
	public Profile saveOrUpdateProfile(Profile profile) {
		if(StringUtils.isEmpty(profile.getAvatar())){
			 String avatar= configureUtil.getConfig(Constants.IMAGE_HOST)+"default"+File.separator+"default_avatar.png";
			 profile.setAvatar(avatar);
		}
		return profileRepository.save(profile);
	}

	@Override
	public Profile createProfile(User u) {
		Profile profile = new Profile();
		profile.setUserId(u.getUuid());
		profile.setStatus(1);
		profile.setType("");
		profile.setRank(1);
		Profile p=u.getProfile();
		if(p==null){
			profile.setNickname("");
			profile.setGender(0);
		    String avatar= configureUtil.getConfig(Constants.IMAGE_HOST)+"default"+File.separator+"default_avatar.png";
			profile.setAvatar(avatar);
		}else{
			profile.setNickname(p.getNickname());
			profile.setGender(p.getGender());
			profile.setAvatar(p.getAvatar());
		}
		profile.setIntegral(0);//设置积分
		profile.setLevel(1); //设置等级
		profile.setUserId(u.getUuid()); //设置反向关联userId
		return profileRepository.save(profile);
	}

	@Override
	public int updateProfile2Connect(Profile profile,String userId) {
		return qCloudService.setUserInfo(profile.getUserId(), profile.getNickname(), profile.getAvatar())?0:-1;
	}

	@Override
	public Profile findByUserId(String userId) {
		return profileRepository.findByUserId(userId);
	}
}
