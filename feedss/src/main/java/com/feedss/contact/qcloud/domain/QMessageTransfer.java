package com.feedss.contact.qcloud.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.feedss.contact.controller.model.Notification;
import com.feedss.contact.entity.CustomMessageContent;
import com.feedss.contact.entity.FaceMessageContent;
import com.feedss.contact.entity.GroupMessage;
import com.feedss.contact.entity.Message;
import com.feedss.contact.entity.MessageContent;
import com.feedss.contact.entity.TextMessageContent;
import com.feedss.contact.entity.MessageContent.MessageContentType;
import com.feedss.contact.qcloud.QCloudMessageUtil;
import com.feedss.contact.qcloud.domain.PushMessageRequestBody.QCondition;

import lombok.Data;

@Component
public class QMessageTransfer {

	/**
	 * 群消息转换
	 * @param msg
	 * @param qMsg
	 */
	public void transfer(GroupMessage msg, GroupMsgRequestBody qMsg){
		qMsg.setFrom_Account(msg.getFromAccount());
		qMsg.setGroupId(msg.getGroupId());
		qMsg.setRandom(QCloudMessageUtil.getRandom());
		List<QMessageElement> eleList = get(msg.getMsgBody());
		qMsg.setMsgBody(eleList);
	}
	
	/**
	 * p2p消息转换
	 * @param msg
	 * @param qMsg
	 */
	public void transfer(Message msg, BatchSendMessageRequestBody qMsg){
		qMsg.setFrom_Account(msg.getFromAccount());
		qMsg.setTo_Account(msg.getToAccount());
		qMsg.setMsgRandom(QCloudMessageUtil.getRandom());
		List<QMessageElement> eleList = get(msg.getMsgBody());
		qMsg.setMsgBody(eleList);
	}
	
	/**
	 * 类型消息转换，把所有其他类型的消息体内容组合起来放到custom消息体中
	 * @param msg
	 * @param qMsg
	 */
	public void transfer(Message msg, PushMessageRequestBody qMsg){
		qMsg.setFrom_Account(msg.getFromAccount());
		//换成attr，按属性推送
		if(msg instanceof GroupMessage){
			GroupMessage groupMessage = (GroupMessage) msg;
			QCondition qCondition = qMsg.new QCondition();
			
			Map<String, String> map = new HashMap<String, String>();
			map.put(QCloudMessageUtil.ATTR_GROUP, groupMessage.getGroupId());
			qCondition.setAttrsAnd(map);
			qMsg.setCondition(qCondition);
		}
		qMsg.setMsgLifeTime(5*60);
		qMsg.setMsgRandom(QCloudMessageUtil.getRandom());
		qMsg.setMsgBody(get(msg.getMsgBody()));
	}
	
	/**
	 * notification消息转换
	 * @param msg
	 * @param qMsg
	 */
	public void transfer(Notification notification, BatchSendMessageRequestBody qMsg){
		qMsg.setTo_Account(notification.getToAccount());
		qMsg.setMsgRandom(QCloudMessageUtil.getRandom());
		List<QMessageElement> list = new ArrayList<QMessageElement>();
		
		QMessageElement textEle = new QMessageElement();
		textEle.setMsgType(MessageContentType.TIMTextElem.name());
		QTextMessageContent textContent = new QTextMessageContent();
		textContent.setText(notification.getContent());
		textEle.setMsgContent(textContent);
		list.add(textEle);
		
		QMessageElement customEle = new QMessageElement();
		customEle.setMsgType(MessageContentType.TIMCustomElem.name());
		QCustomMessageContent customContent = new QCustomMessageContent();
		customContent.setExt(notification.getExt());
		customEle.setMsgContent(customContent);
		list.add(customEle);
		
		qMsg.setMsgBody(list);
	}
	
	/**
	 * notification消息转换
	 * @param msg
	 * @param qMsg
	 */
	public void transfer(Notification notification, PushMessageRequestBody qMsg){
		qMsg.setMsgRandom(QCloudMessageUtil.getRandom());
		List<QMessageElement> list = new ArrayList<QMessageElement>();
		
		QMessageElement textEle = new QMessageElement();
		textEle.setMsgType(MessageContentType.TIMTextElem.name());
		QTextMessageContent textContent = new QTextMessageContent();
		textContent.setText(notification.getContent());
		textEle.setMsgContent(textContent);
		list.add(textEle);
		
		QMessageElement customEle = new QMessageElement();
		customEle.setMsgType(MessageContentType.TIMCustomElem.name());
		QCustomMessageContent customContent = new QCustomMessageContent();
		customContent.setExt(notification.getExt());
		customEle.setMsgContent(customContent);
		list.add(customEle);
		
		qMsg.setMsgBody(list);
	}
	
	private List<QMessageElement> get(List<MessageContent> fromList){
		List<QMessageElement> eleList = new ArrayList<QMessageElement>();
		if(fromList==null || fromList.isEmpty()){
			return eleList;
		}
		for(MessageContent sourceContent:fromList){
			QMessageElement toElement = new QMessageElement();
			toElement.setMsgType(sourceContent.getType());
			if(sourceContent.getType().equals(MessageContent.MessageContentType.TIMTextElem.name())){
				TextMessageContent sourceContent_text = (TextMessageContent) sourceContent;
				QTextMessageContent toContent = new QTextMessageContent();
				toContent.setText(sourceContent_text.getText());
				toElement.setMsgContent(toContent);
			}else if(sourceContent.getType().equals(MessageContent.MessageContentType.TIMFaceElem.name())){
				FaceMessageContent sourceContent_face = (FaceMessageContent) sourceContent;
				QFaceMessageContent toContent = new QFaceMessageContent();
				toContent.setIndex(sourceContent_face.getIndex());
				toContent.setData(sourceContent_face.getData());
				toElement.setMsgContent(toContent);
			}else if(sourceContent.getType().equals(MessageContent.MessageContentType.TIMCustomElem.name())){
				CustomMessageContent sourceContent_custom = (CustomMessageContent) sourceContent;
				QCustomMessageContent toContent = new QCustomMessageContent();
				toContent.setData(sourceContent_custom.getData());
				toContent.setDesc(sourceContent_custom.getDesc());
				toContent.setExt(sourceContent_custom.getExt());
				toElement.setMsgContent(toContent);
			}
			eleList.add(toElement);
		}
		return eleList;
	}
	
	
	@Data
	public class QMessageElement{
		private String MsgType;
		private QMessageContent MsgContent;
	}

	@Data
	public class QMessageContent {
		
	}
	@Data
	public class QTextMessageContent extends QMessageContent{
		private String Text;
	}
	@Data
	public class QFaceMessageContent extends QMessageContent{
		private int Index;
		private String Data;
	}
	@Data
	public class QCustomMessageContent extends QMessageContent{
		private String Desc;
		private String Data;
		private String Ext;
		private String Sound;
	}
}
