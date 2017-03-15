package com.feedss.user.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.JsonData;
import com.feedss.user.entity.Favorite;
import com.feedss.user.entity.Favorite.FavoriteType;
import com.feedss.user.model.FavoriteVo;
import com.feedss.user.repository.FavoriteRepository;
import com.feedss.user.service.UserFavoriteService;

/**
 * 收藏
 * @author 张杰
 *
 */
@Service
public class FavoriteServiceImpl implements UserFavoriteService {

	@Autowired
	FavoriteRepository favoriteRepository;
	
	@Autowired
	private EntityManager entityManager;
	
	@Override
	public Favorite add(String userId,String object,String objectId, String type, String extAttr){
		if(isExist(userId, object, objectId, type)){
			return null;
		}
		Favorite favorite=new Favorite();
		favorite.setUserId(userId);
		favorite.setObject(object);
		favorite.setObjectId(objectId);
		favorite.setType(type);
		favorite.setExtAttr(extAttr);
		return favoriteRepository.save(favorite);
	}
	
	@Override
	@Transactional
	public int del(String userId,String object,String objectId, String type){
		return favoriteRepository.updateStatus(userId, object, objectId, type);
	}
	
	
	@Override
	@Transactional
	public int del(String uuid){
		return favoriteRepository.updateStatus(uuid);
	}
	
	
	@Override
	public Long userFavoritCount(String userId,String object, String type){
	Map<String, Object> paramMap=new HashMap<String, Object>();
	String sql = "select count(f.uuid) from "+Favorite.class.getSimpleName()+" f "
			+ "where f.userId=:userId and status=0 and type=:type ";
	paramMap.put("userId", userId);
	paramMap.put("type", type);
	
	if(StringUtils.isNotEmpty(object)){
		sql+= " and f.object=:object ";
		paramMap.put("object", object);
	}
	sql+= " order by f.created desc ,f.uuid";
	
	TypedQuery<Long> query = entityManager.createQuery(sql,Long.class);
	for(String key:paramMap.keySet()){
		query.setParameter(key, paramMap.get(key));
	}
	Long count=query.getSingleResult().longValue();
	return count==null?0:count;
}
	
	
	@Override
	public List<FavoriteVo> pageUserFavorit(String userId,int size,String cursorId,String object,String type, String direction){
		Map<String, Object> paramMap=new HashMap<String, Object>();

		Date time=DateTime.parse("2000-01-01",DateTimeFormat.forPattern("yyyy-MM-dd")).toDate();
		String sql = "select p from Favorite p where p.userId=:userId and p.status=0 and type=:type  ";
		paramMap.put("type", type);

		if(StringUtils.isNotEmpty(object)){
			sql+= " and p.object=:object ";
			paramMap.put("object", object);
		}
		if(StringUtils.isNotBlank(cursorId)){
			//查出当前光标位置对象
			Favorite favorite=favoriteRepository.getOne(cursorId);
			time=favorite.getCreated();
			if("pre".equals(direction)){
				sql = sql +"  and ( p.created>:time or (p.created=:time and p.uuid>:uuid) ) order by p.created,p.uuid";
			}else{
				sql =  sql +"  and ( p.created<:time or (p.created=:time and p.uuid<:uuid) ) order by p.created desc,p.uuid desc";
			}
			paramMap.put("time", time);
			paramMap.put("uuid", cursorId);
		}else{
			sql =  sql +" order by p.created desc,p.uuid desc";
		}
		paramMap.put("userId", userId);


		TypedQuery<Favorite> query = entityManager.createQuery(sql,Favorite.class)
				.setFirstResult(0).setMaxResults(size);
		for(String key:paramMap.keySet()){
			query.setParameter(key, paramMap.get(key));
		}


		List<Favorite> list = query.getResultList();
		if(list==null){
			list=new ArrayList<Favorite>();
		}
		if(StringUtils.isNotBlank(cursorId) && "pre".equals(direction)){
			Collections.reverse(list);
		}
		List<FavoriteVo> list_vo=new ArrayList<FavoriteVo>();
		for(Favorite f:list){
			list_vo.add(new FavoriteVo(f));
		}
		
		return list_vo;
	}
	
	@Override
	public List<Map<String, Object>> IsFavorite(String userId,
			List<Map<String, String>> list){
		List<Favorite> userFavorit=favoriteRepository.getByUserId(userId, FavoriteType.FAVORITE.name());
		List<Map<String, Object>> result=new ArrayList<Map<String,Object>>();
		Map<String, Integer> map=new HashMap<String, Integer>();
		for(Favorite f:userFavorit){
			map.put(f.getObject()+"_"+f.getObjectId(), 1);
		}
		
		for(int i=0;i<list.size();i++){
			Map<String, String> tmp=list.get(i);
			String object=tmp.get("object");
			String objectId=tmp.get("objectId");
			String mark=object+"_"+objectId;
			
			Map<String,Object> m=new HashMap<String, Object>();
			m.put("object", object);
			m.put("objectId", objectId);
			if(map.get(mark)!=null&&map.get(mark)>0){
				m.put("isFavorite", 1);
			}else{
				m.put("isFavorite", 0);
			}
			result.add(m);
		}
		return result;
	}
	
	@Override
	public boolean isExist(String userId,String object,String objectId, String type){
		int count=favoriteRepository.isExist(userId, object, objectId, type);
		return count>0?true:false;
	}
	@Override
	public Favorite detail(String uuid){
		return favoriteRepository.findOne(uuid);
	}
	
	@Override
	public Favorite detail(String userId,String object,String objectId, String type){
		return favoriteRepository.findFavorite(userId, object, objectId, type);
	}

	@Override
	public List<Map<String, Object>> IsFavorite(String userId, JSONArray array) {
		List<Favorite> userFavorit=favoriteRepository.getByUserId(userId, FavoriteType.FAVORITE.name());
		List<Map<String, Object>> result=new ArrayList<Map<String,Object>>();
		Map<String, Integer> map=new HashMap<String, Integer>();
		for(Favorite f:userFavorit){
			map.put(f.getObject()+"_"+f.getObjectId(), 1);
		}
		
		for(int i=0;i<array.size();i++){
			JSONObject json=array.getJSONObject(i);
			String object=json.getString("object");
			String objectId=json.getString("objectId");
			String mark=object+"_"+objectId;
			
			Map<String,Object> m=new HashMap<String, Object>();
			m.put("object", object);
			m.put("objectId", objectId);
			if(map.get(mark)!=null&&map.get(mark)>0){
				m.put("isFavorite", 1);
			}else{
				m.put("isFavorite", 0);
			}
			result.add(m);
		}
		return result;
	}

	@Override
	public JsonData getFavorites(String userId,int pageSize,String cursorId,String object,String type, String direction) {
		if(StringUtils.isEmpty(type)){
			type = FavoriteType.FAVORITE.name();
		}
		
		if(cursorId==null){
			cursorId="";
		}
		Map<String, Object> data=new HashMap<String, Object>();
		List<FavoriteVo> list=pageUserFavorit(userId, pageSize, cursorId,object, type, direction);
		Long count=userFavoritCount(userId, object, type);
		data.put("list", list);
		data.put("totalCount", count);
		return JsonData.successNonNull(data);
	}
	
	
}
