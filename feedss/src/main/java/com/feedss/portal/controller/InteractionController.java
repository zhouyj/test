package com.feedss.portal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonData;
import com.feedss.content.entity.Stream;
import com.feedss.content.service.RoomService;
import com.feedss.content.service.StreamService;
import com.feedss.portal.entity.Interaction;
import com.feedss.portal.service.InteractionService;
import com.feedss.user.entity.Account;
import com.feedss.user.entity.User.UserStatus;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.AccountService;
import com.feedss.user.service.UserService;

@Controller
@RequestMapping("/interaction")
public class InteractionController extends BaseController {

	@Autowired
	StreamService streamService;
	@Autowired
	InteractionService interactionService;
	@Autowired
	UserService userService;
	@Autowired
	AccountService accountService;
	@Autowired
	RoomService roomService;
    
    @ResponseBody
    @RequestMapping(value = "/publish", method = RequestMethod.POST)
    public JsonData publish(HttpServletRequest request, @RequestBody String body, @RequestHeader String userId) {
        JSONObject param = JSON.parseObject(body);
        String description = param.getString("description");
        int price = param.getIntValue("price");
        String type = param.getString("type");
        String streamId = param.getString("streamId");
        
        if (StringUtils.isEmpty(description) || description.length() < 1) {
            return JsonData.fail(ErrorCode.INVALID_PARAMETERS, "参数不合法，描述太短");
        }
        if(price <= 0){
        	return JsonData.fail(ErrorCode.INVALID_PARAMETERS, "参数不合法，价格需大于0");
        }
        
        UserVo user = userService.getUserVoByUserId(userId);
        if(user==null || user.getStatus()!=UserStatus.NORMAL.ordinal()){
        	return JsonData.fail(ErrorCode.INVALID_PARAMETERS, "参数不合法，用户不存在或被冻结");
        }
        Account account = accountService.getByUserId(userId);
        if(account.getBalance()<price){
        	return JsonData.fail(ErrorCode.BALANCE_NOT_ENOUGH);
        }
                
        if(StringUtils.isEmpty(type)){
        	return JsonData.fail(ErrorCode.INVALID_PARAMETERS, "参数不合法，缺少类型参数");
        }
        if(StringUtils.isEmpty(streamId)){
        	return JsonData.fail(ErrorCode.INVALID_PARAMETERS, "参数不合法，缺少直播信息");
        }
        Stream stream = streamService.selectStream(streamId);
        if(stream==null/* || stream.getStatus()!=StreamStatus.Publishing.ordinal() */){
        	return JsonData.fail(ErrorCode.INVALID_PARAMETERS, "参数不合法，直播不存在或直播已结束");
        }
        if(userId.equals(stream.getUserId())){
        	return JsonData.fail(ErrorCode.INVALID_PARAMETERS, "参数不合法，不能自己给自己发任务或投标");
        }
        Interaction i = interactionService.publish(user, userService.getUserVoByUserId(stream.getUserId()), type, price, stream, description);
    	JsonData r = new JsonData(ErrorCode.SUCCESS);
    	r.addData("interraction", i);
        return r;
    }
    

    @ResponseBody
    @RequestMapping(value = "/confirmFinish", method = RequestMethod.POST)
    public JsonData confirmFinish(HttpServletRequest request, @RequestBody String body, @RequestHeader String userId) {
    	JSONObject param = JSON.parseObject(body);
        String interactionId = param.getString("interactionId");
        ErrorCode e = interactionService.confirmFinish(userId, interactionId);
        return JsonData.fail(e);
    }

    @ResponseBody
    @RequestMapping(value = "/reject", method = RequestMethod.POST)
    public JsonData reject(HttpServletRequest request, @RequestBody String body, @RequestHeader String userId) {
    	JSONObject param = JSON.parseObject(body);
        String interactionId = param.getString("interactionId");
        ErrorCode e = interactionService.reject(userId, interactionId);
        return JsonData.fail(e);
    }

    @ResponseBody
    @RequestMapping(value = "/del", method = RequestMethod.POST)
    public JsonData del(HttpServletRequest request, @RequestBody String body, @RequestHeader String userId) {
    	JSONObject param = JSON.parseObject(body);
        String interactionId = param.getString("interactionId");
        ErrorCode e = interactionService.del(userId, interactionId);
        return JsonData.fail(e);
    }

    @ResponseBody
    @RequestMapping(value = "/listByStream", method = RequestMethod.POST)
    public JsonData listByStream(HttpServletRequest request, @RequestBody String body, @RequestHeader String userId) {
    	JSONObject param = JSON.parseObject(body);
    	String streamId = param.getString("streamId");
    	String type = param.getString("type");
    	
    	Integer pageSize = param.getInteger("pageSize");// 每页显示数量
		pageSize = pageSize == null ? 20 : pageSize;
		String directionStr = param.getString("direction");// 翻页反向 next：下页 pre：上页
		String cursorId = param.getString("cursorId");// 当前游标

		int direction = 1;// 1：更新 2：下一页
		if (StringUtils.isEmpty(directionStr) || directionStr.equals("next")) {
			direction = 2;
		}
    	
    	Stream stream = streamService.selectStream(streamId);
        if(stream==null){
        	return JsonData.fail(ErrorCode.INVALID_PARAMETERS, "参数不合法，直播不存在");
        }
        List<Interaction> list = new ArrayList<>();
        if(userId.equals(stream.getUserId())){//主播查询
        	list = interactionService.select(userId, type, streamId, false, cursorId, pageSize, direction);
        }else{
        	list = interactionService.select(userId, type, streamId, true, cursorId, pageSize, direction);
        }
        JsonData r = new JsonData(ErrorCode.SUCCESS);
        if(list==null){
        	list = new ArrayList<>();
        }
        r.addData("list", list);
        return r;
    }
    
    /**
     * 按stream区分
     * @param request
     * @param body
     * @param userId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public JsonData list(HttpServletRequest request, @RequestBody String body, @RequestHeader String userId) {
    	JSONObject param = JSON.parseObject(body);
    	String type = param.getString("type");
    	boolean isCreator = param.getBooleanValue("isCreator");
    	
    	Integer pageSize = param.getInteger("pageSize");// 每页显示数量
		pageSize = pageSize == null ? 20 : pageSize;
		String directionStr = param.getString("direction");// 翻页反向 next：下页 pre：上页
		String cursorId = param.getString("cursorId");// 当前游标

		int direction = 1;// 1：更新 2：下一页
		if (StringUtils.isEmpty(directionStr) || directionStr.equals("next")) {
			direction = 2;
		}

		JsonData r = new JsonData(ErrorCode.SUCCESS);
        
		List<Map<String, Object>> streamList = new ArrayList<Map<String, Object>>();
        List<String> streamIdList = interactionService.getRelatedStream(userId, type, isCreator, cursorId, pageSize, direction);

		if (streamIdList != null && !streamIdList.isEmpty()) {
			for (String s : streamIdList) {
				Map<String, Object> map = roomService.getBasicRoomInfo(s);
				streamList.add(map);
			}
		}
		
        if(streamList!=null && !streamList.isEmpty()){
        	Map<String, Object> first = streamList.get(0);
        	if(first!=null){
        		List<Interaction> list = interactionService.select(userId, type, (String)first.get("uuid"), isCreator, null, 100, 1);
        		r.addData("list", list);
        	}
        }
        r.addData("streamList", streamList);
        return r;
    }
}
