package com.feedss.user.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.feedss.user.entity.Comment;
import com.feedss.user.entity.Profile;
import com.feedss.user.model.CommentVo;
import com.feedss.user.repository.CommentRepository;
import com.feedss.user.repository.ProfileRepository;
import com.feedss.user.repository.UserRepository;
import com.feedss.user.service.CommentService;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by qinqiang on 2016/8/6.
 */
@Service
public class CommentServiceImpl implements CommentService{

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private ProfileRepository profileRepository;

	@Override
	public Comment createComment(String userId, String object, String objectId, String title, String content) {
		Comment comment = new Comment(userId,object,objectId,title,content);
		return commentRepository.save(comment);
	}

	@Override
	public List<Comment> findComments(String objectId, String cursorId, int pageSize, String direction) {
		Page<Comment> page = null;
		Sort.Direction der = Sort.Direction.DESC;
		if("pre".equals(direction)){
			der = Sort.Direction.ASC;
		}
		Sort sort = new Sort(der,"created");
		sort = sort.and(new Sort(der,"uuid"));
		PageRequest pageParam = new PageRequest(0,pageSize,sort);
		//没有cursorId 获取最新数据
		if(StringUtils.isBlank(cursorId)){
			page= commentRepository.findNewestComments(objectId,pageParam);
		}else{
			//查出当前光标位置对象
			Comment comment = commentRepository.findOne(cursorId);
			Date startDate = comment.getCreated();
			Page<Comment> pages= null;
            if(direction.equals("pre")){
	            page = commentRepository.findPreComments(objectId,startDate,cursorId,pageParam);
            }else{
	            page = commentRepository.findNextomments(objectId,startDate,cursorId,pageParam);
            }
		}
		return page.getContent();
	}

	@Override
	public List<CommentVo> queryComments(String objectId, String cursorId, int pageSize, String direction) {
		String sql = "select p from Comment p where p.objectId=:objectId ";
		Date startDate = null;
		if(StringUtils.isNotBlank(cursorId)){
			//查出当前光标位置对象
			Comment comment = commentRepository.findOne(cursorId);
			 startDate = comment.getCreated();
			if("pre".equals(direction)){
				sql = sql +"  and ( p.created>:startDate or (p.created=:startDate and p.uuid>:uuid) ) order by p.created,p.uuid";
			}else{
				sql =  sql +"  and ( p.created<:startDate or (p.created=:startDate and p.uuid<:uuid) ) order by p.created desc,p.uuid desc";
			}
		}else{
			sql =  sql +" order by p.created desc,p.uuid desc";
		}
		TypedQuery<Comment> query = entityManager.createQuery(sql,Comment.class).setFirstResult(0)
				.setMaxResults(pageSize);
		query.setParameter("objectId",objectId);
		if(StringUtils.isNotBlank(cursorId)){
			query.setParameter("uuid",cursorId);
			query.setParameter("startDate",startDate, TemporalType.TIMESTAMP);
		}
		List<Comment> comments = query.getResultList();
		if(StringUtils.isNotBlank(cursorId) && "pre".equals(direction)){
			Collections.reverse(comments);
		}
		List<CommentVo> commentVos = new ArrayList<CommentVo>();
		for(Comment comment : comments){
			String userId = comment.getUserId();
			Profile profile = profileRepository.findByUserId(userId);
			String userNickName = null;
			if(profile!=null){
				userNickName = profile.getNickname();
			}
			commentVos.add(CommentVo.comment2Vo(comment,userNickName));
		}
		return commentVos;
	}

	@Override
	public List<Comment> findAll() {
		String sql = "select c from Comment c ";
		Query query =entityManager.createQuery(sql);
		return query.getResultList();
	}
}
