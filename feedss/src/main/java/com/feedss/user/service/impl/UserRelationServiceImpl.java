package com.feedss.user.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.transaction.Transactional;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.util.CommonUtil;
import com.feedss.base.util.RedisUtil;
import com.feedss.base.util.RedisUtil.KeyType;
import com.feedss.contact.entity.Message.Source;
import com.feedss.contact.service.MessageService;
import com.feedss.user.entity.User;
import com.feedss.user.entity.UserRelation;
import com.feedss.user.entity.UserRelation.RelationType;
import com.feedss.user.model.UserRelationVO;
import com.feedss.user.model.UserVo;
import com.feedss.user.repository.UserRelationRepository;
import com.feedss.user.service.UserRelationService;
import com.feedss.user.service.UserService;

/**
 * 用户关系
 * @author 张杰
 *
 */
@Service
public class UserRelationServiceImpl implements UserRelationService {

	Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired
	UserRelationRepository userRelationRepository;
	
	@Autowired
	UserService userService;
	
	@Autowired
	RedisUtil redisUtil;
	
	@Autowired
	MessageService messageService;
	
	@Override
	public Map<String, Object> xRank(String curUserId,
			int type,int pageNo,int pageSize){
		Map<String, Object> map=new HashMap<String, Object>();
		String startTime = null;
		String msg="";
		if(type==3){
			msg="本年";
			startTime=new DateTime().dayOfYear().withMinimumValue().toString("yyyy-MM-dd 00:00:00");
		}else if(type==2){
			msg="本月";
			startTime = new DateTime().dayOfMonth().withMinimumValue().toString("yyyy-MM-dd 00:00:00");
		}else{
			msg="本周";
			startTime = new DateTime().dayOfWeek().withMinimumValue().toString("yyyy-MM-dd 00:00:00");
		}
		Integer count=userRelationRepository.xRankCount(startTime);
		JSONArray list=new JSONArray();
		if(count!=null&&count.intValue() > 0){
			pageNo = pageNo < 1 ? 1:pageNo;
			List<Object[]> result=
					userRelationRepository.xRank(startTime,(pageNo-1) * pageSize,pageSize);
			for(Object[] info:result){
				String userId=(String)info[0];
				BigInteger followNum=(BigInteger)info[1];		
				UserVo userVo=userService.getUserVoByUserId(userId);
				boolean isFollow=exist(curUserId, userId, RelationType.FOLLOW.name());
				
				JSONObject obj=new JSONObject();
				JSONObject user=new JSONObject();
				
				user.put("isFollow", isFollow?1:0);
				user.put("describe", msg+"粉丝+"+followNum.intValue());
				user.put("uuid", userId);
				
				String nickName="";
				String avatar="";
				if(userVo.getProfile()!=null){
					nickName=userVo.getProfile().getNickname();
					avatar=userVo.getProfile().getAvatar();
				}
				
				
				JSONObject profile=new JSONObject();
				profile.put("nickname", nickName);
				profile.put("avatar", avatar);
				
				user.put("profile", profile);
				
				obj.put("user", user);
				list.add(obj);
			}
		}
		map.put("list", list);
		map.put("totalCount", count);
		return map;
		
	}
	
	
	@Override
	public List<UserRelationVO> userFollow(String curUserId,String userId,String type
			,String cursorId,int size){
		Date time=DateTime.parse("2999-01-01",DateTimeFormat.forPattern("yyyy-MM-dd")).toDate();
		if(StringUtils.isNotEmpty(cursorId)){
			UserRelation ur=userRelationRepository.getOne(cursorId);
			DateTime dateTime = new DateTime(ur.getCreated());
			time=dateTime.toDate();
		}else{
			cursorId="";
		}
		
		//获取要查询用户列表
		Pageable pageable=new PageRequest(0, size);
		Page<UserRelation> pageList=userRelationRepository.moreByFromUserId(userId, type, cursorId , time , pageable);
		List<UserRelation> list=pageList.getContent();
		
		Map<String, UserRelationVO> followMap=new HashMap<String,UserRelationVO>();
		List<UserRelationVO> list_vo=new ArrayList<UserRelationVO>();
		if(list==null||list.size()<1){
			return list_vo;
		}
		String[] userIds=new String[list.size()];
		for(int i=0;i<list.size();i++){
			UserRelation ur=list.get(i);
			UserRelationVO vo=new UserRelationVO(ur);
			list_vo.add(vo);
			userIds[i]=ur.getToUserId();
			followMap.put(ur.getToUserId(), vo);
		}
		
		String list_key="temp_"+UUID.randomUUID();
		redisUtil.addToSet(KeyType.TempUserList, list_key ,60, userIds);
		System.out.println("显示 列表："+ArrayUtils.toString(redisUtil.membersToSet(KeyType.TempUserList,list_key).toArray()) );

		
		//当前用户：关系主体操作
		Set<String> toFollowSet=null; 
		if(!curUserId.equals(userId)){
			long from_count=userRelationRepository.getCountByFromUserId(curUserId, type);
			long from_cache_count=redisUtil.getCountToSet(KeyType.UserFollowing, curUserId);
			if(from_count!=from_cache_count){
				initFromUid(curUserId, type);
			}
			//列表用户 与当前用户 关注列表的交集
			System.out.println("关注 ："+ArrayUtils.toString(redisUtil.membersToSet(KeyType.UserFollowing,curUserId).toArray()) );
			toFollowSet=redisUtil.intersectionToSet(KeyType.TempUserList, list_key,KeyType.UserFollowing, curUserId);//获取关注用户交集
			for(String followInter :toFollowSet){
				UserRelationVO vo=followMap.get(followInter);
				if(vo!=null){
				vo.setFollow(1);
				}else{
					redisUtil.removeMemberToSet(KeyType.UserFollowing, curUserId, followInter);
					redisUtil.removeMemberToSet(KeyType.UserFollowedBy,followInter,curUserId );
				}
			}
		}else{
			for(UserRelationVO vo:list_vo){
				vo.setFollow(1);
			}
		}
		
		
		//当前用户：关系客体操作
		Set<String> fromFollowSet=null; 
		long to_count=userRelationRepository.getCountByToUserId(curUserId, type);
		long to_cache_count=redisUtil.getCountToSet(KeyType.UserFollowedBy, curUserId);
		if(to_count!=to_cache_count){
			initToUid(curUserId, type);
		}
		//列表用户 与 当前用户 被关注列表的交集
		logger.debug("被 关注 ："+ArrayUtils.toString(redisUtil.membersToSet(KeyType.UserFollowedBy,curUserId).toArray()) );
		fromFollowSet=redisUtil.intersectionToSet(KeyType.TempUserList, list_key,KeyType.UserFollowedBy, curUserId);//获取关注用户交集
		for(String followInter :fromFollowSet){
			UserRelationVO vo=followMap.get(followInter);
			if(vo!=null){
				vo.setFollowBy(1);
			}else{
				redisUtil.removeMemberToSet(KeyType.UserFollowing, curUserId, followInter);
				redisUtil.removeMemberToSet(KeyType.UserFollowedBy,followInter ,curUserId );
			}
		}
		logger.debug(JSONArray.toJSONString(list_vo));
		return list_vo;
	}
	
	
	@Override
	public List<UserRelationVO> userFollowBy(String curUserId,String userId,String type
			,String cursorId,int size){
		Date time=DateTime.parse("2999-01-01",DateTimeFormat.forPattern("yyyy-MM-dd")).toDate();
		if(StringUtils.isNotEmpty(cursorId)){
			UserRelation ur=userRelationRepository.getOne(cursorId);
			DateTime dateTime = new DateTime(ur.getCreated());
			time=dateTime.toDate();
		}else{
			cursorId="";
		}
		
		//获取要查询用户列表
		Pageable pageable=new PageRequest(0, size);
		Page<UserRelation> pageList=userRelationRepository.moreByToUserId(userId, type, cursorId , time , pageable);
		List<UserRelation> list=pageList.getContent();
		
		Map<String, UserRelationVO> followMap=new HashMap<String,UserRelationVO>();
		List<UserRelationVO> list_vo=new ArrayList<UserRelationVO>();
		if(list==null||list.size()<1){
			return list_vo;
		}
		String[] userIds=new String[list.size()];
		for(int i=0;i<list.size();i++){
			UserRelation ur=list.get(i);
			UserRelationVO vo=new UserRelationVO(ur);
			list_vo.add(vo);
			userIds[i]=ur.getFromUserId();
			followMap.put(ur.getFromUserId(), vo);
		}
		
		String list_key="temp_"+UUID.randomUUID();
		redisUtil.addToSet(KeyType.TempUserList, list_key ,60, userIds);
		System.out.println("显示 列表："+ArrayUtils.toString(redisUtil.membersToSet(KeyType.TempUserList,list_key).toArray()) );
		
		//当前用户：关系客体操作
		if(!curUserId.equals(userId)){
			Set<String> fromFollowSet=null; 
			long to_count=userRelationRepository.getCountByToUserId(curUserId, type);
			long to_cache_count=redisUtil.getCountToSet(KeyType.UserFollowedBy, curUserId);
			if(to_count!=to_cache_count){
				initToUid(curUserId, type);
			}
			System.out.println("被 关注 ："+ArrayUtils.toString(redisUtil.membersToSet(KeyType.UserFollowedBy,curUserId).toArray()) );
			fromFollowSet=redisUtil.intersectionToSet(KeyType.TempUserList, list_key,KeyType.UserFollowedBy, curUserId);//获取关注用户交集
			for(String followInter :fromFollowSet){
				UserRelationVO vo=followMap.get(followInter);
				if(vo!=null){
					vo.setFollowBy(1);
				}else{
					redisUtil.removeMemberToSet(KeyType.UserFollowing, curUserId, followInter);
					redisUtil.removeMemberToSet(KeyType.UserFollowedBy,followInter ,curUserId );
				}
			}
		}else{
			for(UserRelationVO vo:list_vo){
				vo.setFollowBy(1);
			}
		}
		
		//当前用户：关系主体操作
		Set<String> fromSet=null; 
		long from_count=userRelationRepository.getCountByFromUserId(curUserId, type);
		long from_cache_count=redisUtil.getCountToSet(KeyType.UserFollowing, curUserId);
		if(from_count!=from_cache_count){
			initFromUid(curUserId, type);
		}
		//列表 与 当前用户 关注列表的交集
		System.out.println("关注 ："+ArrayUtils.toString(redisUtil.membersToSet(KeyType.UserFollowing,curUserId).toArray()) );
		fromSet=redisUtil.intersectionToSet(KeyType.TempUserList, list_key,KeyType.UserFollowing, curUserId);//获取关注用户交集
		for(String followInter :fromSet){
			UserRelationVO vo=followMap.get(followInter);
			if(vo!=null){
				vo.setFollow(1);
			}else{
				redisUtil.removeMemberToSet(KeyType.UserFollowing, curUserId, followInter);
				redisUtil.removeMemberToSet(KeyType.UserFollowedBy,followInter ,curUserId );
			}
		}
		System.out.println(JSONArray.toJSONString(list_vo));
		return list_vo;
	}
	
	
	/**
	 * 初始关系主体
	 * @param userId
	 * @param type
	 */
	private void initFromUid(String fromUserId,String type){
		List<String> toUserIds=userRelationRepository.findToUserIdByFromUserId(fromUserId, type);
		
		if(!redisUtil.exists(KeyType.UserFollowing, fromUserId)){
			String new_=UUID.randomUUID().toString();
			redisUtil.addToSet(
					KeyType.TempUserList, new_, 60, toUserIds.toArray(new String[0]));//数据库新数据
			
			//新旧数据交集
			Set<String> inter= redisUtil.intersectionToSet(
					KeyType.TempUserList, new_ ,KeyType.UserFollowing,fromUserId);
			//旧数据处理
			Set<String> old=redisUtil.membersToSet(KeyType.UserFollowing,fromUserId);
			old.removeAll(inter);//删除旧数据中与新数据交叉的部分
			for(String o:old){
				redisUtil.removeMemberToSet(KeyType.UserFollowing,fromUserId, o);
				redisUtil.removeMemberToSet(KeyType.UserFollowedBy, o, fromUserId);
			}
			//添加新数据
			for(String toUserId:toUserIds){
				redisUtil.addToSet(KeyType.UserFollowing,fromUserId, toUserId);
				redisUtil.addToSet(KeyType.UserFollowedBy, toUserId, fromUserId);
			}
		}else{
			for(String toUserId:toUserIds){
				//判断redis中是否存在关系，不存在需要从数据库获取
				redisUtil.addToSet(KeyType.UserFollowing,fromUserId, toUserId);
				redisUtil.addToSet(KeyType.UserFollowedBy, toUserId, fromUserId);
			}
		}
	}
	/**
	 * 初始关系客体
	 * @param userId
	 * @param type
	 */
	private void initToUid(String toUserId,String type){
		List<String> fromUserIds=userRelationRepository.findFromUserIdByToUserId(toUserId, type);
		if(!redisUtil.exists(KeyType.UserFollowedBy, toUserId)){
			String new_=UUID.randomUUID().toString();
			redisUtil.addToSet(
					KeyType.TempUserList, new_, 60, fromUserIds.toArray(new String[0]));//数据库新数据
			
			//新旧数据交集
			Set<String> inter= redisUtil.intersectionToSet(
					KeyType.TempUserList, new_ ,KeyType.UserFollowedBy,toUserId);
			//旧数据处理
			Set<String> old=redisUtil.membersToSet(KeyType.UserFollowedBy,toUserId);
			old.removeAll(inter);//删除旧数据中与新数据交叉的部分
			for(String o:old){
				redisUtil.removeMemberToSet(KeyType.UserFollowing, o , toUserId);
				redisUtil.removeMemberToSet(KeyType.UserFollowedBy, toUserId ,o );
			}
			//添加新数据
			for(String fromUserId:fromUserIds){
				redisUtil.removeMemberToSet(KeyType.UserFollowing,fromUserId, toUserId);
				redisUtil.removeMemberToSet(KeyType.UserFollowedBy, toUserId ,fromUserId );
			}
		}else{
			for(String fromUserId:fromUserIds){
				//判断redis中是否存在关系，不存在需要从数据库获取
				redisUtil.addToSet(KeyType.UserFollowing,fromUserId, toUserId);
				redisUtil.addToSet(KeyType.UserFollowedBy, toUserId ,fromUserId );
			}
		}
	}
	
	
	
	@Override
	@Transactional
	public UserRelation add(String fromUserId,String toUserId,String type,String groupId){
		UserRelation ur=new UserRelation();
		ur.setCreated(DateTime.now().toDate());
		ur.setFromUserId(fromUserId);
		ur.setToUserId(toUserId);
		
		User fromUser=userService.detail(fromUserId);
		ur.setFromUser(fromUser);
		
		User toUser=userService.detail(toUserId);
		ur.setToUser(toUser);
		
		ur.setType(type);
		
		Object o=CommonUtil.getKey(fromUserId+":"+toUserId);
		synchronized (o) {
			try{
				//a关注b，如果b在a的黑名单中，去掉
				if(exist(fromUserId, toUserId, RelationType.BLACK.name())){
					if(del(toUserId, fromUserId, RelationType.BLACK.name())<=0){
						logger.error("delete black error, from = " + fromUserId + ", to = " + toUserId);
					}
				}
				
				boolean existUr=exist(fromUserId, toUserId, type);
				if(existUr){
					return null;
				}
				if(type.equals(RelationType.FOLLOW.name())){
					redisUtil.addToSet(KeyType.UserFollowing,ur.getFromUserId(), ur.getToUserId());
					redisUtil.addToSet(KeyType.UserFollowedBy, ur.getToUserId(), ur.getFromUserId());
					
					if(StringUtils.isNotEmpty(groupId)){
						Map<String, Object> ext =new HashMap<>();
						ext.put("groupId", groupId);
						ext.put("messageSource", Source.Follow.name());
						ext.put("accountId", fromUserId);
						ext.put("text", fromUser.getProfile().getNickname()+"关注了主播");
//						messageApi.sendMessage(fromUserId,userToken, groupId, "TIMCustomElem", messageBody);
						messageService.sendGroupMessage(groupId, "", ext, "");
					}
				}else if(type.equals(RelationType.BLACK.name())){
					redisUtil.addToSet(KeyType.BlackList, ur.getFromUserId(), ur.getToUserId());
				}
				return userRelationRepository.save(ur);
				
			}catch(Exception e){
				logger.error(e.toString(),e);
				return null;
			}
		}
	}
	
	
	@Override
	public UserRelation detail(String uuid){
		return userRelationRepository.findOne(uuid);
	}
	
	@Override
	public UserRelation detail(String toUserId,String fromUserId,String type){
		return userRelationRepository.find(fromUserId, toUserId, type);
	}
	
	@Override
	@Transactional
	public int del(String userId,String fromUserId,String type){
//		UserRelation ur=detail(userId, fromUserId, type);
		if(type.equals(RelationType.FOLLOW.name())){
			//判断redis中是否存在关系，不存在需要从数据库获取
			redisUtil.removeMemberToSet(KeyType.UserFollowing,fromUserId, userId);
			redisUtil.removeMemberToSet(KeyType.UserFollowedBy,userId, fromUserId);
			
//			String userToken=redisUtil.get(KeyType.TOKEN, fromUserId);
//			contentApi.follow(fromUserId, userToken, userId, 0);
			
		}else if(type.equals(RelationType.BLACK.name())){
			redisUtil.removeMemberToSet(KeyType.BlackList, fromUserId, userId);
		}
		
//		return userRelationRepository.updateStatus(fromUserId, userId, type,1);
		return userRelationRepository.delete(fromUserId, userId, type);
	}

	@Override
	public boolean exist(String fromUserId, String toUserId, String type) {
		UserRelation ur=userRelationRepository.find(fromUserId, toUserId, type);
		if(ur==null){
			return false;
		}else{
			return true;
		}
	}

	@Override
	public long followCount(String userId,String type){
		return userRelationRepository.getCountByFromUserId(userId, type);
	}
	
	@Override
	public long followByCount(String userId,String type){
		return userRelationRepository.getCountByToUserId(userId, type);
	}
	
	
	
}
