package com.feedss.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.feedss.FeedssApplication;
import com.feedss.user.entity.User;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.ProfileService;
import com.feedss.user.service.UserRelationService;
import com.feedss.user.service.UserService;

/**
 * Created by qinqiang on 2016/7/30.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@SpringApplicationConfiguration(FeedssApplication.class)
public class UserServiceTest {
	@Autowired
	private ProfileService profileService;
	
	@Autowired
	private UserRelationService userRelationService;
	
	@Autowired
	private UserService userService;
	
	@Test
	public void refresh(){
		userService.refreshHostList();
	}
	
	@Test
	public void testGetHostList(){
		List<UserVo> list = userService.getHostList(null, "a97d8a1d-800c-437a-ab3b-7a38b4add17b", 2, 2);
		System.out.println("======================");
		for(UserVo u:list){
			System.out.println("u, " + u.getUuid() + ", " + u.getProfile().getNickname() + ", " + u.getFollowByCount());
		}
		List<UserVo> list2 = userService.getHostList(null, "ac0cede1-1338-4c14-b08c-a7f21074cc8a", 2, 2);
		System.out.println("======================");
		for(UserVo u:list2){
			System.out.println("u, " + u.getUuid() + ", " + u.getProfile().getNickname() + ", " + u.getFollowByCount());
		}
		
		List<UserVo> list3 = userService.getHostList(null, "ac0cede1-1338-4c14-b08c-a7f21074cc8a", 2, 1);
		System.out.println("======================");
		for(UserVo u:list3){
			System.out.println("u, " + u.getUuid() + ", " + u.getProfile().getNickname() + ", " + u.getFollowByCount());
		}
	}
	
	@Test
    public void getUserInfo(){
    	List<String> list = readFileByLines("/Users/zhouyujuan/Desktop/3.log");
    	Set<String> set = new HashSet<>();
    	for(String s:list){
    		String[] arr = s.split(",");
    		String time = arr[0];
    		String userId = arr[1].substring(7, arr[1].length());
    		
    		if(set.contains(userId)) continue;
    		set.add(userId);
    		User u = userService.getUserById(userId);
    		String mobile = u.getMobile();
    		if(u.getMobile()==null) {
    			
    			mobile = "xxxxxxxxxxx";
    		}
    		System.out.println(time + "\t" + mobile + "\t" + u.getProfile().getNickname());
    	}
    }
	
		/**
	     * 以行为单位读取文件，常用于读面向行的格式化文件
	     */
	    public static List<String> readFileByLines(String fileName) {
	    	List<String> list = new ArrayList<String>();
	        File file = new File(fileName);
	        BufferedReader reader = null;
	        try {
	            System.out.println("以行为单位读取文件内容，一次读一整行：");
	            reader = new BufferedReader(new FileReader(file));
	            String tempString = null;
	            int line = 1;
	            // 一次读入一行，直到读入null为文件结束
	            while ((tempString = reader.readLine()) != null) {
	                // 显示行号
//	                System.out.println("line " + line + ": " + tempString);
	                line++;
	                list.add(tempString);
	            }
	            reader.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                }
	            }
	        }
	        return list;
	    }
	
	
}
