package com.feedss.content.monitor.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.feedss.base.Constants;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.contact.entity.Message;
import com.feedss.contact.service.MessageService;
import com.feedss.content.entity.Stream;
import com.feedss.content.entity.Stream.StreamStatus;
import com.feedss.content.model.room.Room;
import com.feedss.content.monitor.Listeners;
import com.feedss.content.service.RoomService;
import com.feedss.content.service.StreamService;
import com.feedss.portal.entity.PushStreamTerminal;
import com.feedss.portal.service.TerminalService;

/**
 * @author wangjingqing
 */
@Component(value="streamListeners")
public class StreamListeners implements Listeners<List<String>>{

	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private RoomService roomService;
	@Autowired
	private StreamService streamService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private TerminalService terminalService;
	@Autowired
	private ConfigureUtil configureUtil;
	@Override
	public List<String> onStart(String streamId) {
		//查询所有的推流直播
		List<Stream> streams = streamService.findPublishingStream();
		List<String> streamIds = new ArrayList<String>();
		for(Stream stream:streams){
			int status = stream.getStreamStatus();//当前状态
			if(status == StreamStatus.Publishing.ordinal()){//推流中
				//step:1  获取房间room信息，更新时间
				Room room = roomService.getRoom(stream.getUuid());
				if(room != null){
					room.setUpdateTime(new Date());
					roomService.createRoom(room);
				}else{
					logger.info("StreamListeners->监控room不存在："+stream.getUuid());
				}
				Iterable<Stream> childs = streamService.findConnectings(stream.getUserId(), stream.getUuid());
				for(Stream childStream:childs){
					if(childStream.getStreamStatus() == StreamStatus.Publishing.ordinal()){//判断是否推流中
						Room childRoom = roomService.getChildRoom(childStream.getUserId(), childStream.getParentId());
						if(childRoom != null){
							childRoom.setUpdateTime(new Date());
							roomService.addChildRoom(childRoom);
						}
					}else if(isOutTime(childStream)){
						streamIds.add(childStream.getUuid());
					}
				}
			}else if(status == StreamStatus.Ended.ordinal() && isOutTime(stream)){//结束推流
				//如果是设备推流不做异常保护，由客户端完成结束推流处理
				PushStreamTerminal t = terminalService.getByStreamId(stream.getUuid());
				if(t!=null){
					continue;
				}
				
				streamIds.add(stream.getUuid());
				//close room status
				Room room = roomService.getRoom(stream.getUuid());
				if(room != null){
					room.setUpdateTime(new Date());
					room.setStatus(2);
					roomService.createRoom(room);
					Map<String, Object> extInfo = new HashMap<String, Object>();// 扩展信息
					extInfo.put("groupId", room.getGroupId());
					extInfo.put("playbackUri", stream.getPlaybackUri());
					extInfo.put("messageSource", Message.Source.StreamEnded.name());
//					pushService.push(null, room.getGroupId(), JSONObject.toJSONString(extInfo));
					messageService.sendGroupMessage(room.getGroupId(), "", extInfo, null);
				}
			}
		}
		if(streamIds.size() > 0){//结束stream状态
			logger.info("StreamListeners->异常结束视频:"+streamIds);
			streamService.closeStreams(streamIds); 
		}
		return null;
	}
	
	public boolean isOutTime(Stream stream){
		int roomOutTime = configureUtil.getConfigIntValue(Constants.ROOM_OUT_TIME);
		int status = stream.getStreamStatus();
		if(status == StreamStatus.Ended.ordinal()){
			long second = (new Date().getTime() - stream.getStreamUpdateDate().getTime())/1000;
			if(second > roomOutTime){
				return true;
			}
		}
		return false;
	}
}
