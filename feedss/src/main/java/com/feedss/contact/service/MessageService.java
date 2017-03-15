package com.feedss.contact.service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.feedss.contact.controller.model.Notification;
import com.feedss.contact.entity.CustomMessageContent;
import com.feedss.contact.entity.FaceMessageContent;
import com.feedss.contact.entity.GroupMessage;
import com.feedss.contact.entity.Message;
import com.feedss.contact.entity.MessageContent;
import com.feedss.contact.entity.SystemMessage;
import com.feedss.contact.entity.TextMessageContent;
import com.feedss.contact.qcloud.QCloudMessageUtil;
import com.feedss.contact.repository.SysMessageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

@Component
public class MessageService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private QCloudMessageUtil qcloudMessageUtil;

	@Autowired
	private SysMessageRepository sysMessageRepository;

	public boolean sendGroupMessage(String groupId, String text, Map<String, Object> ext, String sender) {
		GroupMessage groupMsg = new GroupMessage();
		groupMsg.setGroupId(groupId);
		groupMsg.setFromAccount(sender);
		List<MessageContent> msgBody = new ArrayList<>();
		if (!StringUtils.isEmpty(text)) {
			TextMessageContent tmc = new TextMessageContent();
			tmc.setText(text);
			msgBody.add(tmc);
		}
		if (ext != null && !ext.isEmpty()) {
			ext.put("groupId", groupId);
			CustomMessageContent cmc = new CustomMessageContent();
			cmc.setExt(JSONObject.toJSONString(ext));
			msgBody.add(cmc);
		}

		groupMsg.setMsgBody(msgBody);
		return sendGroupMessage(groupMsg);
	}

	public boolean sendSystemMessage(String text, Map<String, Object> ext, String[] receivers) {
		List<MessageContent> msgBody = new ArrayList<>();

		if (!StringUtils.isEmpty(text)) {
			TextMessageContent one = new TextMessageContent();
			one.setText(text);
			msgBody.add(one);
		}

		if (ext != null && !ext.isEmpty()) {
			CustomMessageContent cmc = new CustomMessageContent();
			cmc.setExt(JSONObject.toJSONString(ext));
			msgBody.add(cmc);
		}

		Message message = new Message();
		message.setMsgBody(msgBody);
		if (receivers != null && receivers.length > 0) {
			message.setToAccount(receivers);
		}

		return sendSystemMessage(message);
	}

	public boolean sendSystemMessage(String text, Map<String, Object> ext, String receiver) {
		return sendSystemMessage(text, ext, new String[] { receiver });
	}

	public boolean sendSystemMessage(String systemMessageId) {
		try {
			SystemMessage dbData = sysMessageRepository.findOne(systemMessageId);

			Map<String, Object> ext = new HashMap<String, Object>();
			ext.put("messageSource", Message.Source.SystemMessage.name());

			if (sendSystemMessage(dbData.getContent(), ext, new String[] {})) {
				dbData.setStatus(SystemMessage.Status.SENDED.ordinal());
				dbData.setUpdated(new Date());
				sysMessageRepository.save(dbData);
				return true;
			}
		} catch (Exception e) {
			logger.error("sendSystemMessage error", e);
		}
		return false;
	}

	public SystemMessage getSystemMessage(String msgId) {
		return sysMessageRepository.findOne(msgId);
	}

	public List<SystemMessage> getSystemMessage(int pageNo, int pageSize, String keyword) {
		if (pageNo < 1)
			pageNo = 1;
		List<SystemMessage> list = null;
		if (StringUtils.isEmpty(keyword)) {
			list = sysMessageRepository.getListByPage((pageNo - 1) * pageSize, pageSize);
		} else {
			list = sysMessageRepository.getListByPageKeyWord((pageNo - 1) * pageSize, pageSize, "%" + keyword + "%");
		}
		return list;
	}

	public SystemMessage createSystemMessage(String content) {
		SystemMessage one = new SystemMessage();
		one.setContent(content);
		one.setStatus(SystemMessage.Status.INIT.ordinal());
		one.setCreated(new Date());
		one.setUpdated(new Date());
		one = sysMessageRepository.save(one);
		return one;
	}

	public boolean modifySystemMessage(String messageId, String content) {
		SystemMessage dbData = sysMessageRepository.findOne(messageId);
		if (dbData == null || dbData.getStatus() == SystemMessage.Status.SENDED.ordinal()) {
			return false;
		}
		dbData.setContent(content);
		dbData.setUpdated(new Date());
		dbData = sysMessageRepository.save(dbData);
		if (dbData == null)
			return false;
		return true;
	}

	public boolean sendGroupMessage(GroupMessage groupMsg) {
		qcloudMessageUtil.sendGroupMessage(groupMsg);
		return true;
	}

	public boolean sendSystemMessage(Message msg) {
		if (StringUtils.isEmpty(msg.getFromAccount())) {
			msg.setFromAccount(qcloudMessageUtil.getSystemMessageIdentifier());
		}
		qcloudMessageUtil.sendSystemMessage(msg);
		return true;
	}
	
	public boolean sendPushMessage(String[] toAccount, String title, String content, Map<String, String> extMap){
		Notification notification = new Notification();
		notification.setToAccount(toAccount);
		notification.setTitle(title);
		notification.setContent(content);
		notification.setExt(JSONObject.toJSONString(extMap));
		return this.sendPushMessage(notification);
	}
	public boolean sendPushMessage(String toAccount, String title, String content, Map<String, String> extMap){
		return this.sendPushMessage(new String[]{toAccount}, title, content, extMap);
	}
	

	public boolean sendPushMessage(Notification notification) {
		qcloudMessageUtil.push(notification);
		return true;
	}

	public enum MessageContentParseType {
		TIMTextElem("TIMTextElem", TextMessageContent.class), TIMFaceElem("TIMFaceElem",
				FaceMessageContent.class), TIMCustomElem("TIMCustomElem", CustomMessageContent.class);

		private String name;
		private Class<? extends MessageContent> classType;

		public Class<? extends MessageContent> getClassType() {
			return classType;
		}

		public void setClassType(Class<? extends MessageContent> classType) {
			this.classType = classType;
		}

		private MessageContentParseType(String name, Class<? extends MessageContent> classType) {
			this.name = name;
			this.classType = classType;
		}

		public static MessageContentParseType getByContentType(String type) {
			for (MessageContentParseType t : values()) {
				if (t.name.equals(type)) {
					return t;
				}
			}
			return null;
		}
	}

	public Gson getMessageContentGson() {
		Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<List<MessageContent>>() {
		}.getType(), new JsonDeserializer<List<MessageContent>>() {
			@Override
			public List<MessageContent> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				List list = new ArrayList<MessageContent>();
				JsonArray ja = json.getAsJsonArray();
				for (JsonElement je : ja) {
					String type = je.getAsJsonObject().get("type").getAsString();
					list.add(context.deserialize(je, MessageContentParseType.getByContentType(type).getClassType()));
				}
				return list;
			}
		}).create();
		return gson;
	}

}
