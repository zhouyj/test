package com.feedss.user.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Years;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.feedss.user.entity.Profile;
import com.feedss.user.entity.Role;
import com.feedss.user.entity.TimeCard;
import com.feedss.user.entity.User;

import lombok.Data;

/**
 * Created by qinqiang on 2016/7/31.
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class UserVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2220009507605259071L;
	private String uuid;
	private String mobile;
	private String email;
	private String token;
	private String thirdpartyId;
	private String thirdpartyName;
	private UserProfileVo profile;
	private String userSig;//消息里面需要 在注册、登录时用到
	private Long followCount;
	private Long followByCount;
	private String mallUserToken; //商城用户token
	private int status;//用户状态
	private long created;
	
	private int isFollow;//是否已关注
	private TimeCard timeCard;	//绑定卡片
	
	private List<HashMap<String,String>>  roles;
	
	private boolean profileIsEmpty = false;
	
	private String timeCardNumber;
	
	private List<String> permissions; //权限列表

	public void initVo(User user){
		this.setUuid(user.getUuid());
		this.setEmail(user.getEmail());
		this.setMobile(user.getMobile());
		this.setToken(user.getToken());
		this.setThirdpartyId(user.getThirdpartyId());
		this.setThirdpartyName(user.getThirdpartyName());
		this.setMallUserToken(user.getMallUserToken());
		this.setStatus(user.getStatus());
		DateTime user_created = new DateTime(user.getCreated());;  
		this.setCreated(user_created.getMillis());
		Profile pro = user.getProfile();
		if(pro!=null){
            UserProfileVo profile1 = new UserProfileVo();
		    profile1.setAvatar(pro.getAvatar());
		    profile1.setNickname(pro.getNickname());
			profile1.setGender(pro.getGender());
			profile1.setLevel(pro.getLevel());
			Date date=pro.getBirthdate();
			int age=getAgeFromBirth(date);
			profile1.setAge(age);
			JSONObject ext = JSONObject.parseObject(pro.getExtAttr());
			Map<String, String> extMap = new HashMap<String, String>();
			if(ext!=null){
				for(String k:ext.keySet()){
					extMap.put(k, ext.getString(k));
				}
				profile1.setExt(extMap);
			}
			if(date!=null){
				DateTime dateTime = new DateTime(date);  
				profile1.setBirthdate(dateTime.toString("yyyy-MM-dd"));
			}else{
				profile1.setBirthdate("");
			}
			
			this.setProfile(profile1);
		}else{
			this.profileIsEmpty = true;
		}
	}
	
	public void initRoles(List<Role> roles){
		//
		Map<String, Object> _map=new HashMap<String, Object>(); 
		for(Role role:roles){
			String code = role.getCode();
			_map.put(code,1);
		}
		
		List<HashMap<String,String>>  roleMap = new ArrayList<HashMap<String,String>>();
		for(Role role:roles){
		   String name = role.getName();
		   String code = role.getCode();
		   if(code.equals("0000")&&_map.get("0001")!=null){
			   continue;
		   }
		   HashMap<String,String> map = new HashMap<String,String>();
		   map.put("name",name);
		   map.put("code",code);
		   roleMap.add(map);
		}
		this.setRoles(roleMap);
	}
	
	 /**
	 * 
	 * @param birth
	 * @return
	 */
	private static int getAgeFromBirth(Date date) {
	    if (date == null) {
	        return 0;
	    }
	    //时间解析
	    LocalDate birthday = LocalDate.fromDateFields(date);
	    LocalDate now = new LocalDate();
	    Years age = Years.yearsBetween(birthday, now);
	    return age.getYears();
	}
	
	@Override
	public boolean equals(Object other){
		if(other instanceof UserVo){
			UserVo vo = (UserVo) other;
			if(this.uuid.equalsIgnoreCase(vo.getUuid())) return true;
			return false;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return uuid.hashCode();
	}
}
