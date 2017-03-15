package com.feedss.content.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.feedss.base.Pages;
import com.feedss.base.util.DateUtil;
import com.feedss.content.entity.Trailer;
import com.feedss.content.entity.Trailer.TrailerStatus;
import com.feedss.content.entity.UserTrailer;
import com.feedss.content.repository.TrailerRepository;
import com.feedss.content.repository.UserTrailerRepository;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.UserService;

/**
 * 预告服务<br>
 * @author wangjingqing
 * @date 2016-07-25
 * @since 1.0.0
 */
@Component
public class TrailerService {


	@Autowired
	private TrailerRepository trailerRepository;
	@Autowired
	private UserTrailerRepository userTrailerRepository;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private UserService userService;
	
	
	public Trailer add(String trailerId, String playTimeStr, String title, String picUrl, String content, String userId){
		Trailer trailer;
		if(StringUtils.isNotBlank(trailerId)){
			trailer = findByUuid(trailerId);
			if(trailer == null){
				return null;
			}
		}else{
			trailer = new Trailer();
			trailer.setCreated(new Date());
			trailer.setStatus(TrailerStatus.Available.ordinal());
			trailer.setCreator(userId);
			trailer.setRank(1);
		}
		Date playTime = DateUtil.stringToDate(playTimeStr, DateUtil.FORMAT_STANDERD_MINUTE);
		trailer.setUpdated(new Date());
		trailer.setTitle(title);
		trailer.setUserId(userId);
		trailer.setPicUrl(picUrl);
		trailer.setContent(content);
		trailer.setPlayTime(playTime);
		trailer.setAheadTime(DateUtil.getMinuteMove(playTime, 5));//5分钟的预告时间，往前
		trailer.setSendMsg(true);
		return save(trailer);
	}
	
	public List<Trailer> getUserTrailer(String userId){
		Date now = new Date();
		return trailerRepository.selectUserTrailer(userId, DateUtil.getStartTime(now), DateUtil.getEndTime(now));
	}
	
	/**
	 * 分页查询预告<br>
	 */
	public Pages<Trailer> trailerList(Integer pageNo,Integer pageSize){
		Integer count = trailerRepository.selectTrailerCount(new Date());
		List<Trailer> list = new ArrayList<Trailer>();
		//step:1 判断是否存在预告
		if(count > 0){
			Sort sort = new Sort(Direction.ASC,"playTime");//排序
			PageRequest pageRequest = new PageRequest(pageNo, pageSize,sort);//分页查询
			Page<Trailer> page = trailerRepository.selectTrailerList(new Date(),pageRequest);
			list = page.getContent();
		}
		return new Pages<Trailer>(count, list);
	}
	
	/**
	 * 取消用户预约<br>
	 * @param userId
	 * @param trailerId
	 * @return
	 */
	@Transactional(isolation=Isolation.READ_COMMITTED,readOnly=false,propagation=Propagation.REQUIRED,value="transactionManager")
	public Integer deleteUserTrailer(String userId,String trailerId){
		return userTrailerRepository.deleteUserTrailer(userId, trailerId);
	}
	
	/**
	 * 查询用户预约<br>
	 * @param userId
	 * @param trailerId
	 * @return
	 */
	public UserTrailer selectUserTrailer(String userId,String trailerId){
		UserTrailer userTrailer = userTrailerRepository.selectByUserIdAndTrailerId(userId, trailerId);
		if(userTrailer == null){
			return null;
		}
		return userTrailer;
	}
	/**
	 * 查询用户预约<br>
	 * @param userId
	 * @param trailerId
	 * @return
	 */
	public Integer saveUserTrailer(String userId,String trailerId){
		UserTrailer userTrailer = new UserTrailer();
		userTrailer.setUserId(userId);
		userTrailer.setTrailerId(trailerId);
		userTrailer.setDelete(false);
		userTrailerRepository.save(userTrailer);
		return 1;
	}
	/**
	 * 查询用户预约的信息<br>
	 * @param userId
	 * @param trailerIds
	 */
	public List<UserTrailer> selectUserTrailer(String userId,List<String> trailerIds){
		return userTrailerRepository.selectByUserIdAndTrailerId(userId, trailerIds);
	}
	
	/**
	 * 查询用户的当前预告<br>
	 */
	public Trailer selectTrailerEffective(String userId){
		Date newDate = new Date();
		return trailerRepository.selectTrailerEffective(newDate,newDate,userId);
	}
	
	@Transactional
	public Trailer updateTrailer(Trailer trailer){
		return trailerRepository.save(trailer);
	}
	/**
	 * 查询要推送消息预告<br>
	 * @return List<Trailer>
	 */
	public List<Trailer> selectTrailerToSend(){
		Date newData = new Date();
		return trailerRepository.selectTrailerToSend(newData);
	}
	
	/**
	 * 查询预约用户<br>
	 * @param trailerId
	 * @return List<UserTrailer>
	 */
	public List<UserTrailer> selectUserTrailer(String trailerId){
		return userTrailerRepository.selectByTrailerId(trailerId);
	}
	
	@Transactional
	public Integer updateTrailerToSend(List<String> trailerIds){
		return trailerRepository.updateTrailerToSend(trailerIds);
	}
	
	public Trailer findByUuid(String trailerId){
		return trailerRepository.findByUuid(trailerId);
	}
	
	/**
	 * 保存或更新预告信息<br>
	 * @param trailer
	 * @return
	 */
	public Trailer save(Trailer trailer){
		return trailerRepository.save(trailer);
	}
	
	public Integer selectTrailerListCount(Integer status,String trailerId,String title,String userId,Integer pageNo,Integer pageSize){
		Map<String,Object> paraMap = new HashMap<String,Object>();
		StringBuilder sql = new StringBuilder("select count(t.uuid) from Trailer t where t.status=0 ");
		if(status == 1){//有效
			sql.append(" and t.playTime >:playTime");
			paraMap.put("playTime", new Date());
		}else if(status == 2){//无效
			sql.append(" and t.playTime <:playTime");
			paraMap.put("playTime", new Date());
		}
		if(StringUtils.isNotBlank(trailerId)){
			sql.append(" and t.uuid = :trailerId");
			paraMap.put("trailerId", trailerId);
		}
		if(StringUtils.isNotBlank(title)){//标题
			sql.append(" and t.title like :title");
			paraMap.put("title", "%"+title+"%");
		}
		if(StringUtils.isNotBlank(userId)){//标题
			sql.append(" and t.userId = :userId");
			paraMap.put("userId",userId);
		}
		TypedQuery<Long> result = entityManager.createQuery(sql.toString(), Long.class);
		paraMap.forEach((k,v)->{
			result.setParameter(k, v);
		});
		Long count = result.getSingleResult().longValue();//总数量
		return count == null ? 0:count.intValue();
	}
	/**
	 * 查询列表<br>
	 * @param status
	 * @param trailerId
	 * @param title
	 * @param userId
	 * @param pageNo
	 * @param pageSize
	 */
	public List<Trailer> selectTrailerList(Integer status,String trailerId,String title,String userId,Integer pageNo,Integer pageSize){
		Map<String,Object> paraMap = new HashMap<String,Object>();
		StringBuilder sql = new StringBuilder("select t from Trailer t where t.status=0 ");
		if(status == 1){//有效
			sql.append(" and t.playTime >:playTime");
			paraMap.put("playTime", new Date());
		}else if(status == 2){//无效
			sql.append(" and t.playTime <:playTime");
			paraMap.put("playTime", new Date());
		}
		if(StringUtils.isNotBlank(trailerId)){
			sql.append(" and t.uuid = :trailerId");
			paraMap.put("trailerId", trailerId);
		}
		if(StringUtils.isNotBlank(title)){//标题
			sql.append(" and t.title like :title");
			paraMap.put("title", "%"+title+"%");
		}
		if(StringUtils.isNotBlank(userId)){//标题
			sql.append(" and t.userId = :userId");
			paraMap.put("userId",userId);
		}
		List<Trailer> list = new ArrayList<>();
		sql.append(" order by t.playTime desc");//排序
		TypedQuery<Trailer> resultList = entityManager.createQuery(sql.toString(), Trailer.class).setFirstResult((pageNo-1) * pageSize).setMaxResults(pageSize);;
		paraMap.forEach((k,v)->{
			resultList.setParameter(k, v);
		});
		list = resultList.getResultList();
		if(list!=null && !list.isEmpty()){
			for(Trailer trailer:list){
				UserVo user = userService.getUserVoByUserId(trailer.getUserId());
				if(user!=null) trailer.setUserNickname(user.getProfile().getNickname());
				trailer.setPlayTiemStr(DateUtil.dateToString(trailer.getPlayTime(), com.feedss.base.util.DateUtil.FORMAT_STANDERD_MINUTE));
				trailer.setTrStatus(trailer.getPlayTime().compareTo(new Date()) >= 0 ? 1:2);
			}
		}
		return  list;
	}
}
