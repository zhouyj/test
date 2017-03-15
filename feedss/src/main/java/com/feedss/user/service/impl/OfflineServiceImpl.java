package com.feedss.user.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feedss.contact.entity.Message;
import com.feedss.contact.service.MessageService;
import com.feedss.user.entity.Offline;
import com.feedss.user.entity.Profile;
import com.feedss.user.repository.OfflineRepostitory;
import com.feedss.user.repository.ProfileRepository;
import com.feedss.user.service.OfflineService;
/**
 * 日志
 * @author 张杰
 *
 */
import com.feedss.user.service.UserService;
@Service
public class OfflineServiceImpl implements OfflineService{

	@Autowired
	OfflineRepostitory OfflineRepostitory;
	
	@Autowired
	ProfileRepository profileRepository;
	
	@Autowired
	UserService userService;
	@Autowired
	MessageService messageService;
	
	@Transactional
	@Override
	public Offline add(String userId,String action,String object,String objectId,
			long duration,String ext){
		Offline offline=new Offline();
		offline.setAction(action);
		offline.setObject(object);
		offline.setObjectId(objectId);
		offline.setDuration(duration);
		offline.setUserId(userId);
		offline.setExt(ext);
		
		int minute=(int) (duration/60);
		if(minute!=0){
			//计算积分
			int integral=0;
			if("liveStreaming".equals(action)){
				integral=minute*3;
			}else if("watch".equals(action)){
				integral=minute;
			}
			offline.setIntegral(integral);
		}
		offline=OfflineRepostitory.save(offline);
		if(minute!=0){
			Profile p=profileRepository.findByUserId(userId);
			//重置等级
			Long liveStreaming= OfflineRepostitory.getIntegral(userId, "liveStreaming");
			Long watch=OfflineRepostitory.getIntegral(userId, "watch");
			liveStreaming=liveStreaming==null?0:liveStreaming;
			watch=watch==null?0:watch;
			long total=liveStreaming+watch;
			int level=1;
			if(total<30){
				level=1;
			}else if(total>=30&&total<150){
				level=2;
			}else if(total>=150&&total<750){
				level=3;
			}else if(total>=750&&total<3750){
				level=4;
			}else if(total>=3750&&total<18750){
				level=5;
			}else if(total>=18750&&total<93750){
				level=6;
			}else{
				level=7;
			}
			profileRepository.updateLevel(userId, level,total);
			if(p.getLevel()!=level){			
				userService.updateCacheUserVo(userId);
//				messageApi.sendSystemMsgGrade(userId, userId, level);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("messageSource", Message.Source.SystemMessage.name());
				messageService.sendSystemMessage("恭喜!成功升至" + level + "级", map, userId);
			}
		}
		return offline;
	}
	
}
