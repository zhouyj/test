package com.feedss.contact.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonData;
import com.feedss.base.Row;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.contact.controller.model.AccountParam;
import com.feedss.contact.controller.model.GroupParam;
import com.feedss.contact.entity.CustomMessageContent;
import com.feedss.contact.entity.Group;
import com.feedss.contact.entity.GroupMessage;
import com.feedss.contact.entity.Message.Source;
import com.feedss.contact.entity.MessageContent;
import com.feedss.contact.qcloud.QCloudMessageUtil;
import com.feedss.contact.repository.GroupRepository;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.UserService;

@Component
public class GroupService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;
    
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private QCloudMessageUtil qCloudMessageUtil;

//    private static final String USER_ENTER_ATTR_NAME = "user:enter:attr";

    private static final String USER_ENTER_GROUP = "user:enter:attr:%s";

    private static final String GROUP_USER_LIST = "group:user:list:%s";

    private static final String BLACK_USER_LIST = "group:black:user:list:%s";

    private static final String SHUTUP_MEMBER = "group:shutup:user:list:%s";

    private static final String GROUP_USER_SCORE = "group:user:score:%s";//groupId

    public static final String GROUP_USER_PROFILE = "group:user:profile:%s";

    @SuppressWarnings("rawtypes")
	@Autowired
    private RedisTemplate redisTemplate;
    
    @Autowired
    private ConfigureUtil configUtil;
    
    @Autowired
    private MessageService messageService;

    public Group createGroup(GroupParam param) {
    	if(param==null) return null;
        String groupId = param.getGroupId();
        Group db = findOneGroup(groupId);
        if (db != null) {
            return db;
        }

        if (StringUtils.isEmpty(param.getGroupId())) {
            groupId = UUID.randomUUID().toString();
        }

        Group one = new Group();
        one.setUuid(groupId);
        one.setType(param.getType());
        one.setOwnerAccount(param.getOwnerAccount());
        one.setName(param.getName());
        one.setCreateTime(new Date());
        db = groupRepository.save(one);
        if (db != null) {
        	qCloudMessageUtil.addGroup(groupId, param.getName(), param.getType(), param.getOwnerAccount());

            long score = System.currentTimeMillis();

            String groupKey = String.format(GROUP_USER_LIST, groupId);//新的group
            redisTemplate.opsForZSet().add(groupKey, param.getOwnerAccount(), score);

            String scoreKey = String.format(GROUP_USER_SCORE, groupId);
            redisTemplate.opsForHash().put(scoreKey, param.getOwnerAccount(), score);

            String key = String.format(USER_ENTER_GROUP, param.getOwnerAccount());//用户所在的组
            redisTemplate.opsForValue().set(key, groupId);
        }
        return db;
    }

    public JsonData userEnterGroup(String groupId, String memberAccount) {

    	Group db = findOneGroup(groupId);
    	if(db==null){
    		return JsonData.fail(ErrorCode.GROUP_NOT_EXIST);
    	}
    	// 判断是否在黑名单
        String blackKey = String.format(BLACK_USER_LIST, groupId);
        if (redisTemplate.opsForSet().isMember(blackKey, memberAccount)) {
            return JsonData.fail(ErrorCode.GET_OUT);
        }
        String shutUpKey = String.format(SHUTUP_MEMBER, groupId);
        if (redisTemplate.opsForSet().isMember(shutUpKey, memberAccount)) {
            return JsonData.fail(ErrorCode.SHUT_UP);
        }
        
    	if(!db.getType().equals(Group.GroupType.AVChatRoom.name())){
    		qCloudMessageUtil.enterGroup(groupId, memberAccount);
    	}
    	String key = String.format(USER_ENTER_GROUP, memberAccount);//用户所在的组
        String oldGroupId = (String) redisTemplate.opsForValue().get(key);
        String groupKey;

        if (oldGroupId != null && oldGroupId.equals(groupId)){
            return JsonData.success();//已经是这个组的了
        }

        if (oldGroupId != null && !oldGroupId.equals(groupId)) {
            groupKey = String.format(GROUP_USER_LIST, oldGroupId);//老的group
            redisTemplate.opsForZSet().remove(groupKey, memberAccount);
        }

        long score = System.currentTimeMillis();

        groupKey = String.format(GROUP_USER_LIST, groupId);//新的group
        redisTemplate.opsForZSet().add(groupKey, memberAccount, score);

        String scoreKey = String.format(GROUP_USER_SCORE, groupId);
        redisTemplate.opsForHash().put(scoreKey, memberAccount, score);

        redisTemplate.opsForValue().set(key, groupId);
        Group group = findOneGroup(groupId);
        if(group!=null && !memberAccount.equals(group.getOwnerAccount())){//非群主发一条直播间消息
        	// 发一条直播间消息
        	sendGroupMessage(Source.AddGroupMemmber, memberAccount, groupId);
        }
        return JsonData.success();
    }

    public boolean userExitGroup(String groupId, String memberAccount) {
        //boolean flag = qCloudMessageUtil.removeUserAttr(USER_GROUP_ATTR, memberAccount);
        //boolean flag = qCloudMessageUtil.forbidGroupUser(groupId, memberAccount, 3600 * 24 * 365 * 10);
        //boolean flag = true;
        //if (flag) {

        String key = String.format(USER_ENTER_GROUP, memberAccount);
        redisTemplate.delete(key);

        String groupKey = String.format(GROUP_USER_LIST, groupId);//remove from group list
        redisTemplate.opsForZSet().remove(groupKey, memberAccount);
        //}

        return true;
    }

    public Group findOneGroup(String groupId) {
        if (StringUtils.isEmpty(groupId)) {
            return null;
        }
        return groupRepository.findOne(groupId);
    }

    public boolean checkIsShutup(String groupId, String memberAccount) {
        String key = String.format(SHUTUP_MEMBER, groupId);
        if (redisTemplate.opsForSet().isMember(key, memberAccount)) {
            return true;
        }
        return false;
    }

    public boolean checkIsAddBlack(String groupId, String memberAccount) {
        String key = String.format(BLACK_USER_LIST, groupId);
        if (redisTemplate.opsForSet().isMember(key, memberAccount)) {
            return true;
        }
        return false;
    }
    
    public List<String> findAllGroup(String type){
    	return groupRepository.findByType(type);
    }
    
    public boolean sendGroupMessage(Source msgSource, String accountId, String groupId) {
        try {
            AccountParam account = new AccountParam();
            UserVo userVo = userService.getUserVoByUserId(accountId);
            if(userVo!=null){
            	account.setAccountId(accountId);
            	account.setNickname(userVo.getProfile().getNickname());
            	account.setPhoto(userVo.getProfile().getAvatar());
            }
            String text = configUtil.getConfig(msgSource.name());
            if (StringUtils.isEmpty(text)) {
                logger.error("get configure tips error, name = " + msgSource.name());
                return false;
            }
            if (msgSource == Source.AddGroupMemmber || msgSource == Source.GetOutGroupMemmber
                    || msgSource == Source.ShutUpGroupMemmber) {
                String nickname = "昵称";
                if (!StringUtils.isEmpty(account.getNickname())) {
                    nickname = account.getNickname();
                }
                text = text.replaceAll("#nickname#", nickname);
            }

            GroupMessage groupMsg = new GroupMessage();
            groupMsg.setGroupId(groupId);
            List<MessageContent> contentList = new ArrayList<MessageContent>();
            CustomMessageContent content = new CustomMessageContent();
            content.setType(MessageContent.MessageContentType.TIMCustomElem.name());
            Map<String, String> extMap = new HashMap<String, String>();
            extMap.put("text", text);
            extMap.put("messageSource", msgSource.name());
            extMap.put("groupId", groupId);
            extMap.put("accountId", accountId);
            content.setExt(JSONObject.toJSONString(extMap));
            contentList.add(content);
            groupMsg.setMsgBody(contentList);
            return messageService.sendGroupMessage(groupMsg);
        } catch (Exception e) {
            logger.error("sendGroupMsg error", e);
        }
        return false;
    }
    
    public JsonData getMemberAccounts(String groupId, String cursorId, int pageSize){
        String groupKey = String.format(GROUP_USER_LIST, groupId);
        String scoreKey = String.format(GROUP_USER_SCORE, groupId);

        Long score = Long.MAX_VALUE;
        if(StringUtils.isNotEmpty(cursorId)){
            score = (Long)redisTemplate.opsForHash().get(scoreKey, cursorId);
            if (score == null) {
                score = Long.MAX_VALUE;
            }
        }

        if(!score.equals(Long.MAX_VALUE)){
            pageSize++;
        }

        Set<String> accounts = redisTemplate.opsForZSet().reverseRangeByScore(groupKey, 0, score, 0, pageSize);
        Set<String> shutupSet = redisTemplate.opsForSet().members(String.format(SHUTUP_MEMBER, groupId));
                
        int total = 0;
        List<Row> list = new ArrayList<>();
        if (accounts != null) {
        	total = accounts.size();
            List<String> idList = new ArrayList<>(accounts);
//            List<Row> users = getUserList(idList);
//            List<UserVo> users = userService.batchUserVo(idList);
            for (int i = 0; i < idList.size(); i++) {
                if(!score.equals(Long.MAX_VALUE) && i == 0){
                    continue;
                }
                Row r = new Row();
                r.put("userId", idList.get(i));
                r.put("isShutUp", shutupSet.contains(idList.get(i)) ? 1 : 0);// 0表示正常；1表示禁言
                UserVo userVo = userService.getUserVoByUserId(idList.get(i));
                if(userVo==null){
                	r.put("nickname", "");
                    r.put("avatar", "");
                    r.put("isVip", 0);
                }else{
                	r.put("nickname", userVo.getProfile().getNickname());
                    r.put("avatar", userVo.getProfile().getAvatar());
                    List<HashMap<String, String>> roles = userVo.getRoles();
                    for (int j = 0; j < roles.size(); j++) {
                    	HashMap<String, String> role = roles.get(j);
                    	if (role.containsKey("code") && "0001".equals(role.get("code"))) {
                            r.put("isVip", 1);
                        }
                    }
                }
                list.add(r);
            }
        }

        Row data = new Row();
        data.put("total", total);
        data.put("accountList", list);// 返回所有用户
        return JsonData.successNonNull(data);
    }
}

