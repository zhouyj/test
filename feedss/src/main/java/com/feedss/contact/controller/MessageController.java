package com.feedss.contact.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.contact.controller.model.Notification;
import com.feedss.contact.entity.GroupMessage;
import com.feedss.contact.entity.Message;
import com.feedss.contact.entity.MessageContent;
import com.feedss.contact.service.GroupService;
import com.feedss.contact.service.MessageService;

@Controller
@RequestMapping("/message")
public class MessageController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MessageService messageService;
	@Autowired
	private GroupService groupService;

	@ResponseBody
	@RequestMapping(value = "/sendGroupMsg", method = RequestMethod.POST)
	public ResponseEntity<Object> sendGroupMsg(HttpServletRequest request, @RequestBody String msgStr) {
		logger.info("request body, msgStr = " + msgStr);
		GroupMessage message = null;
		try {
			message = messageService.getMessageContentGson().fromJson(msgStr, GroupMessage.class);
		} catch (Exception e) {
			logger.error("sendGroupMessage parse error", e);
			return JsonResponse.fail(ErrorCode.INVALIDPARAMETERS);
		}
		if (message == null)
			return JsonResponse.fail(ErrorCode.INVALIDPARAMETERS);
		
		try {
			String groupId = message.getGroupId();
			List<MessageContent> msgBody = message.getMsgBody();
			if (StringUtils.isEmpty(groupId) || msgBody == null || msgBody.isEmpty() || message == null) {
				return JsonResponse.fail(ErrorCode.INVALIDPARAMETERS);
			}

			// 是否被禁言和是否被踢出
			if (groupService.checkIsAddBlack(message.getGroupId(), message.getFromAccount())) {
				logger.error(
						"用户已经被主播踢出，groupId = " + message.getGroupId() + ", and memberId = " + message.getFromAccount());
				return JsonResponse.fail(ErrorCode.GET_OUT);
			}
			if (groupService.checkIsShutup(message.getGroupId(), message.getFromAccount())) {
				logger.error(
						"用户已经被禁言，groupId = " + message.getGroupId() + ", and memberId = " + message.getFromAccount());
				return JsonResponse.fail(ErrorCode.SHUT_UP);
			}
			if (messageService.sendGroupMessage(message)){
				return JsonResponse.success();
			}
			
		} catch (Exception e) {
			logger.error("sendGroupMessage error", e);
		}
		return JsonResponse.fail(ErrorCode.INTERNALERROR);
	}

	@ResponseBody
	@RequestMapping(value = "/sendSystemMsg", method = RequestMethod.POST)
	public ResponseEntity<Object> sendSystemMsg(HttpServletRequest request, @RequestBody String msgStr) {
		logger.info("request body, " + msgStr);
		try {
			Message message = messageService.getMessageContentGson().fromJson(msgStr, Message.class);
			List<MessageContent> msgBody = message.getMsgBody();
			if (msgBody == null || msgBody.isEmpty()) {
				return JsonResponse.fail(ErrorCode.INVALIDPARAMETERS);
			}
			if (messageService.sendSystemMessage(message))
				return JsonResponse.success();
		} catch (Exception e) {
			logger.error("sendSystemMessage error", e);
		}

		return JsonResponse.fail(ErrorCode.INTERNALERROR);
	}

	@ResponseBody
	@RequestMapping(value = "/sendNotification", method = RequestMethod.POST)
	public ResponseEntity<Object> sendNotification(HttpServletRequest request, @RequestBody String notificationStr) {
		logger.info("sendNotification..., " + notificationStr);
		Notification notification = JSONObject.parseObject(notificationStr, Notification.class);
		if (notification == null
				|| (StringUtils.isEmpty(notification.getContent()) && StringUtils.isEmpty(notification.getExt()))) {
			return JsonResponse.fail(ErrorCode.INVALIDPARAMETERS);
		}
		try {
			messageService.sendPushMessage(notification);
		} catch (Exception e) {
			logger.error("sendPushMessage error", e);
		}
		return JsonResponse.success();
	}

}
