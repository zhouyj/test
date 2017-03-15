package com.feedss.contact;

import java.util.Date;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.feedss.FeedssApplication;
import com.feedss.base.JsonData;
import com.feedss.contact.controller.model.GroupParam;
import com.feedss.contact.entity.Group;
import com.feedss.contact.qcloud.QCloudMessageUtil;
import com.feedss.contact.service.GroupService;

/**
 * Created by shenbingtao on 2016/8/2.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FeedssApplication.class)
@WebAppConfiguration
public class GroupTest {

    @Autowired
    private RedisTemplate redisTemplate;

    private static SerializeConfig mapping = new SerializeConfig();
    private static String dateFormat;
    static {
        dateFormat = "yyyy-MM-dd HH:mm:ss";
        mapping.put(Date.class, new SimpleDateFormatSerializer(dateFormat));
    }

    @Autowired
    private GroupService groupService;

    @Autowired
    private QCloudMessageUtil qCloudMessageUtil;

    @Test
    public void testAddGroup() {
        GroupParam param = new GroupParam();
        param.setGroupId("Group678" + System.currentTimeMillis());
        param.setOwnerAccount("c8329ba1-a233-4205-bafd-8bbdd1c1017c");
        param.setName("Group678");
        param.setType("AVChatRoom");
        groupService.createGroup(param);
    }

    @Test
    public void findOneGroup() {
        GroupParam param = new GroupParam();
        param.setGroupId("Group123");
        Group o = groupService.findOneGroup(param.getGroupId());
        System.out.println(JSON.toJSONString(o, true));
    }

    @Test
    public void enterGroup() {
        JsonData resp = groupService.userEnterGroup("Group6781472482253382", "c8329ba1-a233-4205-bafd-8bbdd1c1017c");
        System.out.println(resp.getCode());
    }

    @Test
    public void exitGroup() {
        boolean flag = groupService.userExitGroup("Group6781472482253382", "c8329ba1-a233-4205-bafd-8bbdd1c1017c");
        System.out.println(flag);
    }



    @Test
    public void testImport(){
    	String id = "systemMessageAccount";
    	String nickname = "系统消息";
    	String logo = "http://image.feedss.com/20160810/e6139273-a802-41c0-b753-82239cd669ef.jpg";
        qCloudMessageUtil.importAccount(id, nickname, logo);
        
    }

    @Test
    public void enter() throws Exception{
        String GROUP_USER_LIST = "group:user:list:%s";
        String GROUP_USER_SCORE = "group:user:score:%s";//groupId
        String[] ids = {"b0ceff5d-2c40-450b-ac3c-61959a6944e8","c8329ba1-a233-4205-bafd-8bbdd1c1017c","958274f9-eb30-4a79-a67f-edf7599f7a56",
                "a569cbf4-0a6a-4e5d-9335-a92993e0767f","55ffb5da-9984-4c80-a7c8-aae9ffaae77b","5a62739a-a338-4083-82a4-076bc0a82286",
                "9c4f47ff-050d-4b2c-b064-a0603f2f53a3","c4343b83-79e6-4935-b23f-14a6878982c2","e2d7ee77-1b8f-4e0a-83c1-93ccaaf78bfc",
                "62f87e98-a52e-4400-ada0-fd9220404d2f"};
        String groupId = "Group6781472482253388";
        String groupKey = String.format(GROUP_USER_LIST, groupId);//新的group
        String scoreKey = String.format(GROUP_USER_SCORE, groupId);
        for(String id : ids){
            long score = System.currentTimeMillis();
            redisTemplate.opsForZSet().add(groupKey, id, score);
            redisTemplate.opsForHash().put(scoreKey, id, score);
            Thread.sleep(1000);
        }

    }

    @Test
    public void get(){
        String GROUP_USER_LIST = "group:user:list:%s";
        String GROUP_USER_SCORE = "group:user:score:%s";//groupId

        String groupId = "Group6781472482253382";
        String groupKey = String.format(GROUP_USER_LIST, groupId);//新的group
        Set set = redisTemplate.opsForZSet().range(groupKey,0,-1);
        System.out.println(set);
    }

    @Test
    public void get2(){
        String GROUP_USER_LIST = "group:user:list:%s";
        String GROUP_USER_SCORE = "group:user:score:%s";//groupId

        String groupId = "Group6781472482253382";
        String groupKey = String.format(GROUP_USER_LIST, groupId);//新的group
        Set set = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(groupKey,0,Long.MAX_VALUE,0,2);
        System.out.println(JSON.toJSONString(set));
    }
}
