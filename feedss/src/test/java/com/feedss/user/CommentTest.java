package com.feedss.user;

import com.feedss.FeedssApplication;
import com.feedss.user.entity.Comment;
import com.feedss.user.model.CommentVo;
import com.feedss.user.model.ProductVo;
import com.feedss.user.repository.CommentRepository;
import com.feedss.user.service.CommentService;
import com.feedss.user.service.UsrProductService;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by qinqiang on 2016/7/30.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@SpringApplicationConfiguration(FeedssApplication.class)
public class CommentTest {
	@Autowired
	private CommentService  commentService;

	@org.junit.Test
	public void test(){
       List<CommentVo> comments = commentService.queryComments("123","dd39dc72-a4dc-4e05-a29c-07cf6627fa60",4,"pre");
	   for(CommentVo comment:comments){
		   //System.out.println(comment.getId()+"---"+comment.getCreated().getTime());
	   }
	}
}
