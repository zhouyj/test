package com.feedss.user.service.impl;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feedss.contact.service.MessageService;
import com.feedss.user.entity.Apply;
import com.feedss.user.entity.Apply.ApplyAction;
import com.feedss.user.entity.UserRole;
import com.feedss.user.model.ApplyVo;
import com.feedss.user.repository.ApplyRepository;
import com.feedss.user.service.ApplyService;
import com.feedss.user.service.UserRoleService;
import com.feedss.user.service.UserService;
/**
 * 申请认证
 * @author 张杰
 *
 */
@Service
public class ApplyServiceImpl implements ApplyService {

	@Autowired
	ApplyRepository applyRepository;
	
	@Autowired
	UserRoleService userRoleService;
	
	@Autowired
	private EntityManager entityManager;

	@Autowired
	MessageService messageService;
	
	@Autowired
	UserService userService;
	
	@Override
	public Map<String, Object> getApplyVList(String uuid, String nickname,
			String mobile, String field, String direction, int page, int size){
		Map<String, Object> data = new HashMap<String, Object>();
		String sqlFiled=" a.created ";
		String sqlDirection=" desc ";
		if("age".equals(field)){
			sqlFiled=" p.birthdate ";
		}else if("level".equals(field)){
			sqlFiled=" p.level ";
		}
		
		if("asc".equals(direction)){
			sqlDirection=" asc ";
		}
		if(page<1){
			page=1;
		}
		int start = (page-1)*size;
		
		Map<String, Object> condition=new HashMap<String,Object>();
		StringBuffer sql = new StringBuffer("");
		StringBuffer sqlCount=new StringBuffer("");
		
		sql.append("select a.uuid as uuid ,a.user_id as userId,a.status,a.created,u.mobile,p.gender,"
				+ "p.level,p.avatar,p.birthdate,p.nickname,r.name as roleName");
		sqlCount.append("select count(*) ");
		
		String join=" from apply as a "+
			" left JOIN user as u "+
			" on a.user_id=u.uuid "+
			" left join profile as p "+
			" on u.uuid=p.user_id "+
			" left JOIN role as r "+
			" on a.object_id=r.uuid "+
			" where action='"+ApplyAction.AUTH+"' "+
			" and object='role' ";
		sql.append(join) ;
		sqlCount.append(join) ;
		
		if(StringUtils.isNotEmpty(uuid)){
			sql.append(" and a.user_id =:userId ");
			sqlCount.append(" and a.user_id =:userId ");
			condition.put("userId", uuid);
        }
		
		if(StringUtils.isNotEmpty(nickname)){
			sql.append(" and p.nickname like :nickname ");
			sqlCount.append(" and p.nickname like :nickname  ");
			condition.put("nickname", "%"+nickname+"%");
        }
		
		if(StringUtils.isNotEmpty(mobile)){
			sql.append(" and u.mobile = :mobile ");
			sqlCount.append(" and u.mobile = :mobile ");
			condition.put("mobile", mobile);
        }
		sql.append(" order by "+sqlFiled +" "+sqlDirection);
		
		
		Query query=entityManager.createNativeQuery(sql.toString());
		for(String key:condition.keySet()){
			query.setParameter(key, condition.get(key));
		}
		query.setFirstResult(start).setMaxResults(size);
		query.unwrap(SQLQuery.class).
		setResultTransformer(new AliasToBeanResultTransformer(ApplyVo.class));
		
		List<ApplyVo> list=query.getResultList();
		 
		Query queryCount =entityManager.createNativeQuery(sqlCount.toString());
		for(String key:condition.keySet()){
			queryCount.setParameter(key, condition.get(key));
		}
		long totalCount=((BigInteger)queryCount.getSingleResult()).longValue();
	    
		data.put("list", list);
		data.put("pageNo", page);
		data.put("pageSize", size);
		data.put("totalCount", totalCount);
		data.put("totalPage", totalCount%size==0?totalCount/size:(totalCount/size+1));
		return data;
	}
	
	@Override
	public Apply applyStaus(ApplyAction action,String userId,String object,String objectId){
		List<Apply> list=applyRepository.ApplyStatus(action.name(), object, userId, objectId);
		if(list!=null&&list.size()>0){
			return list.get(0);
		}
		return null; 
	}
	
	@Override
	public Apply apply(String action,String userId,String object,String objectId,String reason){
		Apply apply=new Apply();
		apply.setAction(action);
		apply.setUserId(userId);
		apply.setObject(object);
		apply.setObjectId(objectId);
		apply.setReason(reason);
		return applyRepository.save(apply);
	}
	
	@Transactional
	@Override
	public int reply(String applyId,int status){
		int row=0;
		if(status==1){
			row=applyRepository.updateStatus(applyId, 1);
			Apply apply=applyRepository.findOne(applyId);
			
			UserRole ur=userRoleService.find(apply.getUserId(), apply.getObjectId());
			if(ur==null){//
				userRoleService.save(apply.getUserId(), apply.getObjectId());
				userService.updateCacheUserVo(apply.getUserId());
				//发送消息通知用户
//				messageApi.sendSystemMsgV(apply.getUserId(), apply.getUserId(), "1");
				Map<String, Object> ext = new HashMap<>();
				ext.put("messageSource", "ApplyForAddV");
				ext.put("userId", apply.getUserId());
				ext.put("status", status);
				messageService.sendSystemMessage("恭喜你加V认证成功！", ext, apply.getUserId());
			}
		}else{
			row=applyRepository.updateStatus(applyId, -1);
			//发送消息通知用户
			Apply apply=applyRepository.findOne(applyId);
			UserRole ur=userRoleService.find(apply.getUserId(), apply.getObjectId());
			if(ur==null){
				//发送消息通知用户
//				messageApi.sendSystemMsgV(apply.getUserId(), apply.getUserId(), "0");
				Map<String, Object> ext = new HashMap<>();
				ext.put("messageSource", "ApplyForAddV");
				ext.put("userId", apply.getUserId());
				ext.put("status", status);
				messageService.sendSystemMessage("抱歉，你的加V认证申请未通过。", ext, apply.getUserId());
			}
		}
		return row;
	}
	
	
}
