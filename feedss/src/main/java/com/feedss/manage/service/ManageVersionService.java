package com.feedss.manage.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.feedss.manage.entity.ManageVersion;
import com.feedss.manage.repository.ManageVersionRepository;

/**
 * 版本控制
 * @author 张杰
 *
 */
@Service
public class ManageVersionService {

	@Autowired
	ManageVersionRepository manageVersionRepository;
	
	Logger logger=Logger.getLogger(getClass());
	
	public ManageVersion detail(String uuid){
		return manageVersionRepository.getOne(uuid);
	}
	

	public Map<String, Object> list(final String version,final String channel,int page,int pageSize){
		Map<String, Object> map=new HashMap<String, Object>();
		
		Pageable pageable=new PageRequest(page, pageSize);
		Specification<ManageVersion> spec = new Specification<ManageVersion>() {
				@Override
				public Predicate toPredicate(Root<ManageVersion> root,
						CriteriaQuery<?> query, CriteriaBuilder cb) {
					List<Predicate> list = new ArrayList<Predicate>();
					root.alias("u");
					if (StringUtils.isNotEmpty(version)) {
						list.add(cb.equal(root.get("version").as(String.class), version));
					}
					if (StringUtils.isNotEmpty(channel)) {
						list.add(cb.equal(root.get("channel").as(String.class),channel));
					}
					list.add(cb.equal(root.get("status").as(String.class),0));
					Predicate[] p = new Predicate[list.size()];
					return cb.and(list.toArray(p));
				}
			};
		
		Page<ManageVersion> p=manageVersionRepository.findAll(spec, pageable);
		
		long totalCount=p.getTotalElements();
				
		map.put("totalCount", totalCount);
		map.put("pageNo", page);
		map.put("pageSize", pageSize);
		System.out.println(totalCount);
		int totalPages=(int) (totalCount%pageSize==0?
				totalCount/pageSize:(totalCount/pageSize)+1);
		map.put("totalPages", totalPages);
		
		List<ManageVersion> list=p.getContent();
		map.put("list", list);
		return map;
	}
	
	public List<String> getChannels(){
		return manageVersionRepository.getChannels();
	}
	
	@Transactional
	public JSONObject add(String[] channelInfos, String[] fileUrls, String update_info){
		JSONObject json=new JSONObject();
		for(int i=0;i<channelInfos.length;i++){
			String ch = channelInfos[i].substring(0, channelInfos[i].lastIndexOf("."));
			String[] infos=ch.split("_");//文件名格式必须是：版本_版本号_更新类型_渠道_文件地址
			String version=infos[0];
			String version_code=infos[1];
			String update_type=infos[2];
			String channel=infos[3];
			String fileUrl= fileUrls[i];
			
			ManageVersion mv=new ManageVersion();
			mv.setChannel(channel);
			mv.setCreated(DateTime.now().toDate());
			mv.setDown_url(fileUrl);
			mv.setUpdate_info(update_info);
			mv.setUpdate_type(Integer.parseInt(update_type));
			mv.setDevice_type("");
			mv.setVersion(version);
			mv.setVersion_code(Integer.parseInt(version_code));
			mv.setUpdated(new Date());
			mv =manageVersionRepository.save(mv);
		}
			
			json.put("code", 0);
			json.put("msg", "成功");
		return json;
	}
	
	
	@Transactional
	public int updateStatus(String uuid,int status){
		return manageVersionRepository.updateStatus(uuid, status);
	}
	
	
}
