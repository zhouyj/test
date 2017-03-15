package com.feedss.portal.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feedss.base.Pages;
import com.feedss.base.util.ConvertUtil;
import com.feedss.base.util.DateUtil;
import com.feedss.content.entity.WatchStream;
import com.feedss.content.entity.WatchStream.WatchType;
import com.feedss.content.service.WatchStreamService;
import com.feedss.portal.service.RankService;
import com.feedss.portal.util.StringLength;
import com.feedss.user.entity.UserRelation;
import com.feedss.user.model.UserProfileVo;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.UserRelationService;
import com.feedss.user.service.UserService;

@Service
public class RankServiceImpl implements RankService {

	@Autowired
	private UserService userService;
//	private UserProxyService userProxyService;
	@Autowired
	private WatchStreamService watchStreamService;
	@Autowired
	private UserRelationService userRelationService;
	
	public Map<String, Object> ranking(Integer category, Integer type, String userId, Integer pageNo, Integer pageSize){
		String watchType = "";
		String desc = "";// 描述
		// step:1 判断期间
		Date startTime = new Date();
		if (type.intValue() == 1) {// 本周排行
			startTime = DateUtil.getWeekStart(startTime);
			desc = "本周";
		} else if (type.intValue() == 2) {// 本月排行
			startTime = DateUtil.getMonthStart(startTime);
			desc = "本月";
		} else if (type.intValue() == 3) {// 年度排行
			startTime = DateUtil.getYearStart(startTime);
			desc = "本年";
		}
		// step:2 判断类别
		if (category.intValue() == 2) {// 经验榜 直播时长
			watchType = WatchType.Stream.name();
			desc += "直播时长：";
		} else if (category.intValue() == 3) {// 忠诚榜 观看时长
			watchType = WatchType.WatchStream.name();
			desc += "观看时长：";
		}
		// step:3 查询排行信息
		Pages<WatchStream> pages = watchStreamService.selectRanking(startTime, watchType, pageNo, pageSize);
		Integer totalCount = pages.getTotalCount();// 总数量
		List<Map<String, Object>> reList = new ArrayList<Map<String, Object>>();
		if (totalCount.intValue() > 0) {
			List<WatchStream> list = pages.getList();
			// step:1 获取用户ids
			Set<String> userIds = new HashSet<String>();
			for (WatchStream info : list) {
				userIds.add(info.getCreator());
			}
			// step:2 查询用户信息
			Map<String, UserVo> userInfo = new HashMap<String, UserVo>();
			if(!userIds.isEmpty()){
				List<String> userIdList = new ArrayList<String>();
				userIdList.addAll(userIds);
				List<UserVo> userList = userService.batchUserVo(userIdList);// 批量查询用户信息
				for (UserVo user : userList) {
					userInfo.put(user.getUuid(), user);
				}
			}
			// step:3 遍历用户信息
			for (WatchStream info : list) {
				String creator = info.getCreator();
				UserVo user = userInfo.get(creator);
				String describe = desc + DateUtil.toHour(info.getWatchTime()) + "h";
				reList.add(userToMap(user, userId, describe, true));
			}
		}
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("totalCount", totalCount);
		reMap.put("list", reList);
		return reMap;
	}
	private String getShortNickname(String nickname) {
		if (StringUtils.isNotEmpty(nickname) && StringLength.getLength(nickname) > 8) {
			return nickname.substring(0, 8) + "...";
		}
		return nickname;
	}


	/**
	 * user信息转换成map<br>
	 * 
	 * @param user
	 * @return Map<String,Object>
	 */
	private Map<String, Object> userToMap(UserVo user, String userId, String describe, boolean needShortNickname) {
		Map<String, Object> userMap = new HashMap<String, Object>();
		Map<String, Object> profileMap = new HashMap<String, Object>();
		if (user == null) {
			userMap.put("uuid", "");
			userMap.put("isFollow", 0);
			userMap.put("describe", "");
			profileMap.put("nickname", "");
			profileMap.put("avatar", "");
			profileMap.put("level", 0);
		} else {
			userMap.put("uuid", user.getUuid());
			Integer isFollow = 0;
			if (userId != null) {// 关注
				boolean follow = userRelationService.exist(userId, user.getUuid(), UserRelation.RelationType.FOLLOW.name());
//				boolean follow = userProxyService.isFollow(userId, user.getUuid());
				isFollow = follow ? 1 : 0;
			}
			userMap.put("isFollow", isFollow);
			userMap.put("describe", describe);
			UserProfileVo profile = user.getProfile();
			if (profile == null) {
				profileMap.put("nickname", "");
				profileMap.put("avatar", "");
				profileMap.put("level", 0);
			} else {
				if(needShortNickname){
					profileMap.put("nickname", getShortNickname(ConvertUtil.objectToString(profile.getNickname(), false)));
				}else{
					profileMap.put("nickname", ConvertUtil.objectToString(profile.getNickname(), false));
				}
				profileMap.put("avatar", ConvertUtil.objectToString(profile.getAvatar(), false));
				profileMap.put("level", ConvertUtil.objectToInt(profile.getLevel(), false));
			}
		}
		userMap.put("profile", profileMap);
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("user", userMap);
		return reMap;
	}
	
}
