package com.feedss.contact.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.Row;
import com.feedss.contact.controller.model.GroupParam;
import com.feedss.contact.entity.Group;
import com.feedss.contact.entity.Message.Source;
import com.feedss.contact.qcloud.QCloudMessageUtil;
import com.feedss.contact.service.GroupService;

@RestController
@RequestMapping("/group")
public class GroupController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GroupService groupService;

    private static final String GROUP_USER_LIST = "group:user:list:%s";

    private static final String BLACK_USER_LIST = "group:black:user:list:%s";

    public static final String SHUTUP_MEMBER = "group:shutup:user:list:%s";

    private static final String ZHUBO_STATUS = "group:zhubo:status:%s:%s";
    
    private static final String ONLINE_NUMBER = "all:online:number";
    

    @SuppressWarnings("rawtypes")
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private QCloudMessageUtil qCloudMessageUtil;

    @ResponseBody
    @RequestMapping("/test")
    public String test(HttpServletRequest request) {
        return "Hello World! 你好！";
    }

    /**
     * 创建群
     */
    @ResponseBody
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<Object> createGroup(HttpServletRequest request, @RequestBody String msgStr) {
        GroupParam param = JSON.parseObject(msgStr, GroupParam.class);
        Group group = groupService.createGroup(param);
        return JsonResponse.success(group);
    }

    @ResponseBody
    @RequestMapping(value = "/find", method = RequestMethod.POST)
    public ResponseEntity<Object> find(HttpServletRequest request, @RequestBody String msgStr) {
        GroupParam param = JSON.parseObject(msgStr, GroupParam.class);
        if (param == null) return JsonResponse.fail(ErrorCode.INVALIDPARAMETERS);
        return JsonResponse.success(groupService.findOneGroup(param.getGroupId()));
    }

    @ResponseBody
    @RequestMapping(value = "/enter", method = RequestMethod.POST)
    public ResponseEntity<Object> enter(HttpServletRequest request, @RequestBody String msgStr) {
        GroupParam param = JSON.parseObject(msgStr, GroupParam.class);
        groupService.userEnterGroup(param.getGroupId(), param.getAccountId());
        return JsonResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = "/exit", method = RequestMethod.POST)
    public ResponseEntity<Object> exit(HttpServletRequest request, @RequestBody String msgStr) {
        GroupParam param = JSON.parseObject(msgStr, GroupParam.class);
        boolean flag = groupService.userExitGroup(param.getGroupId(), param.getMemberAccount());
        return flag ? JsonResponse.success() : JsonResponse.fail(ErrorCode.INTERNALERROR);
    }

    @ResponseBody
    @RequestMapping(value = "/getOut", method = RequestMethod.POST)
    public ResponseEntity<Object> getOut(HttpServletRequest request, @RequestBody String msgStr) {
        GroupParam param = JSON.parseObject(msgStr, GroupParam.class);
        // 标记黑名单
        String key = String.format(BLACK_USER_LIST, param.getGroupId());
        redisTemplate.opsForSet().add(key, param.getMemberAccount());

        // 把用户从group中剔除
        boolean flag = groupService.userExitGroup(param.getGroupId(), param.getMemberAccount());
        if (flag) {
            logger.info(String.format("====发一条直播间消息type:%s,userId:%s,groupId%s",
                    Source.GetOutGroupMemmber, param.getMemberAccount(), param.getGroupId()));
            // 发一条直播间消息
            groupService.sendGroupMessage(Source.GetOutGroupMemmber, param.getMemberAccount(), param.getGroupId());
        }
        return flag ? JsonResponse.success() : JsonResponse.fail(ErrorCode.INTERNALERROR);
    }

    @ResponseBody
    @RequestMapping(value = "/shutUpMember", method = RequestMethod.POST)
    public ResponseEntity<Object> shutUpMember(HttpServletRequest request, @RequestBody String msgStr) {
        GroupParam param = JSON.parseObject(msgStr, GroupParam.class);

        qCloudMessageUtil.forbidGroupUser(param.getGroupId(), param.getMemberAccount(), 3600 * 24 * 365 * 10);
        String key = String.format(SHUTUP_MEMBER, param.getGroupId());
        redisTemplate.opsForSet().add(key, param.getMemberAccount());
        groupService.sendGroupMessage(Source.ShutUpGroupMemmber, param.getMemberAccount(), param.getGroupId());
        return JsonResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = "/cancelShutUpMember", method = RequestMethod.POST)
    public ResponseEntity<Object> cancelShutUpMember(HttpServletRequest request, @RequestBody String msgStr) {

        GroupParam param = JSON.parseObject(msgStr, GroupParam.class);
        qCloudMessageUtil.forbidGroupUser(param.getGroupId(), param.getMemberAccount(), 0);
        String key = String.format(SHUTUP_MEMBER, param.getGroupId());
        redisTemplate.opsForSet().remove(key, param.getMemberAccount());

        return JsonResponse.success();
    }


    @ResponseBody
    @RequestMapping(value = "/getMemberAccounts", method = RequestMethod.POST)
    public ResponseEntity<Object> getMemberAccounts(HttpServletRequest request, @RequestBody String msgStr) {
        GroupParam param = JSON.parseObject(msgStr, GroupParam.class);
        return JsonResponse.response(groupService.getMemberAccounts(param.getGroupId(), param.getCursorId(), param.getPageSize()));
    }
    
    @ResponseBody
    @RequestMapping(value = "/getOnlineAccountNumber", method = RequestMethod.POST)
    public ResponseEntity<Object> getOnlineAccountNumber(HttpServletRequest request, @RequestBody String msgStr) {
    	long total = 0;
    	Object totalObj = redisTemplate.opsForValue().get(ONLINE_NUMBER);
    	if(totalObj==null){
    		List<String> groupIds = groupService.findAllGroup(Group.GroupType.AVChatRoom.name());
        	if(groupIds!=null && !groupIds.isEmpty()){
        		for(String groupId:groupIds){
        			String groupKey = String.format(GROUP_USER_LIST, groupId);
        	        long number = redisTemplate.opsForZSet().count(groupKey, 0, Long.MAX_VALUE);
        	        logger.info("getOnlineAccountNumber, groupId = " + groupId + ", number = " + number);
        	        total+=number;
        		}
        	}
        	redisTemplate.opsForValue().set(ONLINE_NUMBER, total, 60,TimeUnit.SECONDS);
    	}else{
    		logger.debug("get from redis, " + totalObj);
    	}
    	total = (Long)totalObj; 
        
        Row data = new Row();
        data.put("total", total);

        return JsonResponse.success(data);
    } 

    @ResponseBody
    @RequestMapping(value = "/uploadHeartbeat", method = RequestMethod.POST)
    public ResponseEntity<Object> uploadHeartbeat(HttpServletRequest request, @RequestBody String msgStr) {
        GroupParam param = JSON.parseObject(msgStr, GroupParam.class);
        if (StringUtils.isEmpty(param.getAccountId())) {
            return JsonResponse.fail(ErrorCode.INVALIDPARAMETERS);
        }
        String key = String.format(ZHUBO_STATUS, param.getGroupId(), param.getAccountId());
        redisTemplate.opsForValue().set(key, 1, 10, TimeUnit.MINUTES);
        return JsonResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = "/getHeartbeat", method = RequestMethod.POST)
    public ResponseEntity<Object> getHeartbeat(HttpServletRequest request, @RequestBody String msgStr) {
        GroupParam param = JSON.parseObject(msgStr, GroupParam.class);
        if (StringUtils.isEmpty(param.getAccountId())) {
            return JsonResponse.fail(ErrorCode.INVALIDPARAMETERS);
        }
        Row row = new Row();
        row.put("online",
                redisTemplate.hasKey(String.format(ZHUBO_STATUS, param.getGroupId(), param.getAccountId())) ? 1 : 0);
        return JsonResponse.success(row);
    }

}
