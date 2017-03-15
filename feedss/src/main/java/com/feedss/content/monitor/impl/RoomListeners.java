package com.feedss.content.monitor.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.feedss.base.Constants;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.content.entity.Stream;
import com.feedss.content.entity.WatchStream.WatchType;
import com.feedss.content.model.room.Room;
import com.feedss.content.model.room.RoomUser;
import com.feedss.content.monitor.Listeners;
import com.feedss.content.service.RoomService;
import com.feedss.content.service.StreamService;
import com.feedss.content.service.WatchStreamService;
import com.feedss.portal.entity.PushStreamTerminal;
import com.feedss.portal.service.TerminalService;

/**
 * 房间监听<br>
 * @author wangjingqing
 * @date 2016-08-06
 */
@Component("roomListeners")
public class RoomListeners implements Listeners<List<String>>{

	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private RoomService roomService;
	@Autowired
	private WatchStreamService watchStreamService;
	@Autowired
	private StreamService streamService;
	@Autowired
	private TerminalService terminalService;
	@Autowired
	private ConfigureUtil configureUtil;
	
	public static List<String> streamIds = new Vector<String>();
	
	@Override
	public List<String> onStart(String streamId) {
		List<String> closeStreamIds = new ArrayList<String>();//移除链接视频
		
		//step:1 获取直播房间
		Room room = roomService.getRoom(streamId);
		if(room == null){
			logger.info("主播房间：["+streamId+"]不存在");
			roomService.removeAudienceAll(streamId);//移除所有观众
	    	roomService.removeRequestConnectAll(streamId);//移除所有请求者
	    	roomService.removeChildRoom(streamId);//移除所有连接
			RoomListeners.deleteListener(streamId);//删除当前的streamId监控
			return null;
		}
		Stream stream = streamService.selectStream(streamId);
		if(stream == null){
			logger.info("主播房间stream：["+streamId+"]不存在");
			RoomListeners.deleteListener(streamId);//删除当前的streamId监控
			return null;
		}
		int status = stream.getStatus();//状态 0：创建 1：推流 2：结束
		
		//step:2 获取所有连接
		List<Room> childRooms = roomService.getChildRoomList(streamId);
		long newLong = new Date().getTime()/1000;//当前时间
		
	    //step:3  判断直播是否3分钟内没有监控
	    if(isOutTime(room.getUpdateTime()) && status != 2){//直播超时
	    	if(status == 0){//创建直播超时
	    		streamService.deleteStream(stream.getUserId(), streamId);//删除创建直播
	    		RoomListeners.deleteListener(streamId);
	    		roomService.removeRoom(streamId);//移除直播
	    	}else{
	    		//如果是设备推流不做异常保护，由客户端完成结束推流处理
				PushStreamTerminal t = terminalService.getByStreamId(stream.getUuid());
				if(t!=null){
					return closeStreamIds;
				}
	    		closeStreamIds.add(streamId);//关闭流状态
	    		List<RoomUser> roomUsers = roomService.getAudienceAll(streamId);
	    		for(RoomUser roomUser : roomUsers){
	    			//记录观看时长
	    			watchStreamService.save(roomUser.getUuid(), streamId,newLong - roomUser.getCreateTime().getTime()/1000, WatchType.WatchStream.name());
	    		}
	    	}
	    	//roomService.removeAudienceAll(streamId);//移除所有观众
	    	roomService.removeRequestConnectAll(streamId);//移除所有请求者
	    	roomService.removeChildRoom(streamId);//移除所有连接
	    	//roomService.removeRoom(streamId);//移除直播
	    	//修改直播状态
	    	room.setStatus(2);
	    	roomService.createRoom(room);
	    	for(Room childRoom:childRooms){
	    		closeStreamIds.add(childRoom.getUuid());
	    	}
    		//记录观看时长
    		watchStreamService.save(room.getRoomUser().getUuid(), streamId, newLong - room.getCreateTime().getTime()/1000, WatchType.Stream.name());
	    	//closeStreamIds.add(streamId);//关闭流状态
	    }else{//未超时
	    	//step:4  判断观众是否异常，异常移除
		    List<RoomUser> roomUsers = roomService.getAudienceAll(streamId);
		    for(RoomUser roomUser : roomUsers){
		    	Date updateTiem = roomUser.getUpdateTiem()== null ?new Date():roomUser.getUpdateTiem();
		    	if(isOutTime(updateTiem)){
		    		roomService.removeAudience(streamId, roomUser.getUuid());
		    		String type = WatchType.WatchStream.name();
		    		if(status==2){//区分观看直播还是回放
		    			type = WatchType.WatchBack.name();
		    		}
		    		//记录观看时长
		    		watchStreamService.save(roomUser.getUuid(), streamId,newLong - roomUser.getCreateTime().getTime()/1000, type);
		    	}
		    }
		    //step:5 判断子类视频是否异常，异常移除
		    for(Room childRoom:childRooms){
		    	if(isOutTime( childRoom.getUpdateTime())){
		    		closeStreamIds.add(childRoom.getUuid());
		    		roomService.removeChildRoom(childRoom.getRoomUser().getUuid(), streamId);
		    		closeStreamIds.add(childRoom.getUuid());
		    	}
		    }
		    //step:6 判断请求连接
		    List<RoomUser> requestList = roomService.getRequestConnectAll(streamId);
		    for(RoomUser roomUser : requestList){
		    	if(isOutTime( roomUser.getUpdateTiem())){
		    		roomService.removeRequestConnect(streamId, roomUser.getUuid());
		    	}
		    }
	    }
	    return closeStreamIds;
	}

	/**
	 * 判断是否超时<br>
	 * @param update
	 * @return
	 */
	private boolean isOutTime(Date update){
		int roomOutTime = configureUtil.getConfigIntValue(Constants.ROOM_OUT_TIME);
    	long updateTime = update.getTime()/1000;
    	long newTime = new Date().getTime()/1000;
    	if((newTime - updateTime) > roomOutTime){
    		return true;
    	}
    	return false;
	}
	/**
	 * 添加监听streamId<br>
	 */
	public static boolean addStreamId(String streamId){
		return streamIds.add(streamId);
	}
	/**
	 * 移除监听streamId<br>
	 * @param room
	 * @return
	 */
	public static boolean deleteListener(String streamId){
		return streamIds.remove(streamId);
	}
	/**
	 * 移除监听streamId<br>
	 * @param index
	 * @return
	 */
	public static boolean deleteListener(Integer index){
		return streamIds.remove(index);
	}
}
