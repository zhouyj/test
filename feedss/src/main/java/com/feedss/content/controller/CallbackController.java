package com.feedss.content.controller;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.feedss.base.Constants;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.content.controller.model.CallbackEvent;
import com.feedss.content.controller.model.CallbackEvent.ActionType;
import com.feedss.content.entity.Stream;
import com.feedss.content.entity.Stream.StreamStatus;
import com.feedss.content.model.room.Room;
import com.feedss.content.monitor.impl.RoomListeners;
import com.feedss.content.service.RoomService;
import com.feedss.content.service.StreamService;

/**
 * VSS server http callback
 * 
 * @author tangjun
 *
 */
@Controller
public class CallbackController {
	Log logger = LogFactory.getLog(getClass());

	@Autowired
	private StreamService streamService;
	@Autowired
	private RoomService roomService;
	@Autowired
	private ConfigureUtil configureUtil;

	@RequestMapping(value = "/callback", method = RequestMethod.POST, consumes = "application/json")
	public String greeting(@RequestBody CallbackEvent event, Model model) {
		logger.info("callback->" + event);
		try {
			String app = event.getApp();
			if (StringUtils.isEmpty(app) || StringUtils.isEmpty(event.getAction())
					|| StringUtils.isEmpty(event.getStream())) {
				logger.error("callback must contain: app, action, stream");
				return "/callback";
			}

			String APPNAME = configureUtil.getConfig(Constants.APP_NAME);
			if (!app.equals(APPNAME)) {
				logger.error("callback event's app error, should be: " + APPNAME);
				return "/callback";
			}

			Stream stream = roomService.getRelatedStream(event.getStream());
			if (stream == null) {
				logger.info("callback->stream不存在：" + event.getStream());
				return "/callback";
			}
			// 推流
			if (event.getAction().equals(ActionType.on_publish.name())) {
				if (stream.getStatus() == StreamStatus.Ended.ordinal()) {
					logger.info("callback->视频已结束：" + event.getStream() + ", streamId = " + stream.getUuid());
					return "/callback";
				}
				stream.setStreamStatus(StreamStatus.Publishing.ordinal());
				stream.setStatus(StreamStatus.Publishing.ordinal());
				stream.setStreamUpdateDate(new Date());
				stream.setUpdated(new Date());
				streamService.save(stream);
				// 打开room状态 (暂时监控下)
				Room room = roomService.getRoom(stream.getUuid());
				if (room != null) {
					int status = room.getStatus();
					logger.info("callback->当前room状态：" + status + "==" + stream.getStatus());
					if (status == 2 || status == 0) {
						logger.info("callback->打开rooom状态：" + status);
						room.setStatus(StreamStatus.Publishing.ordinal());
						room.setCreateTime(new Date());
						room.setUpdateTime(new Date());
						roomService.createRoom(room);
						RoomListeners.addStreamId(stream.getUuid());
					}
				}
			}
			// 结束推流 有监控来关闭异常结束 只更新流状态和流时间
			if (event.getAction().equals(ActionType.on_unpublish.name())) {
				stream.setStreamUpdateDate(new Date());
				int result = streamService.updateStreamStatus(StreamStatus.Ended.ordinal(), new Date(),
						stream.getUuid());
				if (result <= 0) {
					logger.info("callback->updateStreamStatus failed, " + event.getStream());
				}
			}
			// append此次回访的内容，须将文件路径转为相对地址
			if (event.getAction().equals(ActionType.on_dvr.name())) {
				String file = event.getFile();
				//注意appname存在于file中的情况
				String playBackUrl = file.substring(file.indexOf(APPNAME + "/") - 1);
				file = configureUtil.getConfig(Constants.PLAYBACK_DOMAIN) + playBackUrl;
				stream = streamService.updatePlayBackUri(file, stream);
				logger.info("callback->playbackurl, after updatePlayBackUri, streamId = " + event.getStream()
						+ ", playbackuri = " + stream.getPlaybackUri());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "/callback";
	}
	
}