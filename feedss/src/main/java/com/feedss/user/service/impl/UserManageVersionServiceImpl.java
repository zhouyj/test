package com.feedss.user.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.feedss.user.entity.Version;
import com.feedss.user.repository.VersionRepository;
import com.feedss.user.service.UserManageVersionService;

/**
 * 
 * @author 张杰
 *
 */
@Service
public class UserManageVersionServiceImpl implements UserManageVersionService {

	@Autowired
	VersionRepository manageVersionRepository;
	
	@Override
	public Version getNewVersion(final String channel){

		Pageable pageable=new PageRequest(0, 1);
		Specification<Version> spec = new Specification<Version>() {
				@Override
				public Predicate toPredicate(Root<Version> root,
						CriteriaQuery<?> query, CriteriaBuilder cb) {
					List<Predicate> list = new ArrayList<Predicate>();
					root.alias("u");
					if (StringUtils.isNotEmpty(channel)) {
						list.add(cb.equal(root.get("channel").as(String.class),channel));
					}
					list.add(cb.equal(root.get("status").as(String.class),0));
					Predicate[] p = new Predicate[list.size()];
					return cb.and(list.toArray(p));
				}
			};
		
		Page<Version> p=manageVersionRepository.findAll(spec, pageable);
		List<Version> mvL=p.getContent();
		if(mvL!=null && mvL.size()>0){
			return mvL.get(0);
		}
		return null;
	}
	
}
