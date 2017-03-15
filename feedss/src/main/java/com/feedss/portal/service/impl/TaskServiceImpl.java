package com.feedss.portal.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonData;
import com.feedss.base.Row;
import com.feedss.contact.service.MessageService;
import com.feedss.content.service.StreamService;
import com.feedss.portal.dto.TaskDTO;
import com.feedss.portal.entity.Task;
import com.feedss.portal.entity.Task.TaskStatus;
import com.feedss.portal.entity.TaskAccept;
import com.feedss.portal.entity.TaskReject;
import com.feedss.portal.repository.TaskAcceptRepository;
import com.feedss.portal.repository.TaskRejectRepository;
import com.feedss.portal.repository.TaskRepository;
import com.feedss.portal.service.TaskService;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by shenbingtao on 2016/7/23.
 */
@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

	@Resource
	private TaskRepository taskDao;

	@Resource
	private TaskRejectRepository taskRejectDao;

	@Resource
	private TaskAcceptRepository taskAcceptDao;

	@Resource
	private StreamService streamService;
	@Resource
	private UserService userService;
	@Resource
	private MessageService messageService;

	@Resource
	RedisTemplate redisTemplate;

	private static final String LOCK = "task:rob:lock";

	private static final String MSG_FINISH = "恭喜您！%s已经成功确认完成任务\"%s\"，您可前往您空间的任务列表查看详情。";

	private static final String MSG_REJECT = "%s不同意您完成的任务\"%s\"，理由是\"%s\"，您可前往您空间的任务列表查看详情。";

	private static final String MSG_APPLY = "%s已确认完成任务\"%s\"，请您前往您空间的任务列表确认任务是否成功完成。";

	@Override
	public Task add(String userId, String content, String streamId) {
		Task one = new Task();
		one.setCreator(userId);
		one.setDescription(content);
		one.setStatus(Task.TaskStatus.PUBLIC.ordinal());
		one.setType(0 + "");
		one.setStreamId(streamId);
		Date now = new Date();
		one.setCreated(now);
		one.setUpdated(now);
		return taskDao.save(one);
	}

	@Override
	public Task get(String taskId) {
		return taskDao.findOne(taskId);
	}

	@Override
	@Transactional
	public JsonData rob(String userId, String taskId) {

		// 加锁
		boolean flag = redisTemplate.opsForValue().setIfAbsent(LOCK, System.currentTimeMillis());
		if (flag) {
			redisTemplate.expire(LOCK, 20, TimeUnit.SECONDS);

			Task one = taskDao.findOne(taskId);
			if (one == null) {
				redisTemplate.delete(LOCK);
				return JsonData.fail(ErrorCode.FAIL, "任务不存在");
			}

			if (one.getStatus() != Task.TaskStatus.PUBLIC.ordinal()) {
				redisTemplate.delete(LOCK);
				return JsonData.fail(ErrorCode.FAIL, "任务已被抢");
			}

			if (one.getCreator().equals(userId)) {
				redisTemplate.delete(LOCK);
				return JsonData.fail(ErrorCode.FAIL, "不能抢自己发布的任务");
			}

			TaskAccept ta = new TaskAccept();
			Date now = new Date();
			ta.setCreator(userId);
			ta.setTaskId(taskId);
			ta.setCreated(now);
			ta.setUpdated(now);
			TaskAccept out = taskAcceptDao.save(ta);

			if (out != null) {
				updateTaskStatus(taskId, Task.TaskStatus.ACCEPT.ordinal());
				redisTemplate.delete(LOCK);
				return JsonData.success();
			}
		}

		redisTemplate.delete(LOCK);
		return JsonData.fail(ErrorCode.FAIL, "服务器繁忙请稍后重试");
	}

	@Override
	public boolean applyFinish(String userId, String taskId) {
		Task one = taskDao.findOne(taskId);
		if (one == null || (one.getStatus() != Task.TaskStatus.ACCEPT.ordinal()
				&& one.getStatus() != Task.TaskStatus.REJECT.ordinal())) {
			return false;
		}

		TaskAccept ta = taskAcceptDao.findByTaskIdAndCreator(taskId, userId);
		if (ta == null) {
			return false;
		}

		boolean flag = updateTaskStatus(taskId, Task.TaskStatus.REPLY_FINISH.ordinal());
		log.info("updateTaskStatus rlt:" + flag);
		if (flag) {
			Row user = getUser(userId);
			String nickname = user.gets("nickname", "");
//			pushMessageToUser(String.format(MSG_APPLY, nickname, one.getDescription()), one.getCreator());
			messageService.sendSystemMessage(String.format(MSG_APPLY, nickname, one.getDescription()), null, one.getCreator());
		}
		return flag;
	}

	@Override
	public boolean confirmFinish(String userId, String taskId) {
		Task one = taskDao.findOne(taskId);
		if (one == null || !userId.equals(one.getCreator())
				|| one.getStatus() != Task.TaskStatus.REPLY_FINISH.ordinal()) {
			return false;
		}

		boolean flag = updateTaskStatus(taskId, Task.TaskStatus.FINISH.ordinal());
		if (flag) {
			TaskAccept task = getAccept(taskId);
			log.info("getAccept rlt:" + (task != null ? JSON.toJSONString(task) : ""));
			if (task != null) {
				Row user = getUser(userId);
				String nickname = user.gets("nickname", "");
				messageService.sendSystemMessage(String.format(MSG_FINISH, nickname, one.getDescription()), null, task.getCreator());
			}
		}
		return flag;
	}

	@Override
	public boolean reject(String userId, String taskId, String content) {
		Task one = taskDao.findOne(taskId);
		if (one == null || !userId.equals(one.getCreator())
				|| one.getStatus() != Task.TaskStatus.REPLY_FINISH.ordinal()) {
			return false;
		}

		boolean flag = updateTaskStatus(taskId, Task.TaskStatus.REJECT.ordinal());
		if (flag) {
			addLog(userId, taskId, content);
			TaskAccept task = getAccept(taskId);
			log.info("getAccept rlt:" + (task != null ? JSON.toJSONString(task) : ""));
			if (task != null) {
				Row user = getUser(userId);
				String nickname = user.gets("nickname", "");
				messageService.sendSystemMessage(String.format(MSG_REJECT, nickname, one.getDescription(), content), null, 
						task.getCreator());
			}
		}
		return flag;
	}

	private boolean updateTaskStatus(String taskId, int status) {
		Task one = taskDao.findOne(taskId);
		one.setStatus(status);
		one.setUpdated(new Date());
		return taskDao.save(one) != null;
	}

	private boolean updateTaskType(String taskId, int type) {
		Task one = taskDao.findOne(taskId);
		if (one == null)
			return false;
		one.setType(type + "");
		one.setUpdated(new Date());
		return taskDao.save(one) != null;
	}

	private boolean addLog(String userId, String taskId, String content) {
		TaskReject log = new TaskReject();
		log.setCreator(userId);
		log.setDescription(content);
		Date now = new Date();
		log.setCreated(now);
		log.setUpdated(now);
		log.setTaskId(taskId);
		return taskRejectDao.save(log) != null;
	}

	@Override
	public List<TaskDTO> getPublishTaskList(String userId, String streamId) {
		List<Task> list = streamId == null ? taskDao.getList(userId) : taskDao.getListByStream(userId, streamId);
		List<TaskDTO> lst = new ArrayList<TaskDTO>();
		if (list != null) {
			for (Task one : list) {
				TaskDTO dto = new TaskDTO(one);
				dto.setRejectList(taskRejectDao.getList(one.getUuid()));
				TaskAccept ta = taskAcceptDao.findByTaskId(one.getUuid());
				if (ta != null) {
					Row user = getUser(ta.getCreator());
					if (user != null) {
						dto.setAvatar(user.gets("avatar"));
						dto.setNickname(user.gets("nickname"));
					}
				}
				dto.setStatusDescription(
						TaskDTO.getStatusDescription(one.getStatus(), "<em>" + dto.getNickname() + "</em>"));

				if (StringUtils.isEmpty(dto.getDescription())) {
					dto.setDescription("\t\n\n");
				}
				lst.add(dto);
			}
		}
		return lst;
	}

	@Override
	public List<TaskDTO> getPublishTaskList(String streamId, int status) {
		List<Task> list = taskDao.getListByStream(streamId, status);
		List<TaskDTO> lst = new ArrayList<TaskDTO>();
		if (list != null) {
			for (Task one : list) {
				TaskDTO dto = new TaskDTO(one);
				dto.setRejectList(taskRejectDao.getList(one.getUuid()));
				TaskAccept ta = taskAcceptDao.findByTaskId(one.getUuid());
				if (ta != null) {
					Row user = getUser(ta.getCreator());
					if (user != null) {
						dto.setAvatar(user.gets("avatar"));
						dto.setNickname(user.gets("nickname"));
					}
				}
				dto.setStatusDescription(
						TaskDTO.getStatusDescription(one.getStatus(), "<em>" + dto.getNickname() + "</em>"));

				if (StringUtils.isEmpty(dto.getDescription())) {
					dto.setDescription("\t\n\n");
				}
				lst.add(dto);
			}
		}
		return lst;
	}

	@Override
	public List<TaskDTO> getAcceptTaskList(String userId, String streamId) {
		List<Task> list = streamId == null ? taskDao.getAcceptList(userId)
				: taskDao.getAcceptListByStream(userId, streamId);
		List<TaskDTO> lst = new ArrayList<TaskDTO>();
		if (list != null) {
			for (Task one : list) {
				TaskDTO dto = new TaskDTO(one);
				dto.setRejectList(taskRejectDao.getList(one.getUuid()));
				Row user = getUser(one.getCreator());
				if (user != null) {
					dto.setAvatar(user.gets("avatar"));
					dto.setNickname(user.gets("nickname"));
				}
				dto.setStatusDescription(TaskDTO.getStatusDescription(one.getStatus(), ""));
				lst.add(dto);
			}
		}
		return lst;
	}

	public Row getUser(String userId) {
		if (StringUtils.isEmpty(userId)) {
			return null;
		}
		UserVo userVo = userService.getUserVoByUserId(userId);
		Row r = new Row();
		r.put("uuid", userId);

		if (userVo == null) {
			r.put("nickname", "");
			r.put("avatar", "");
			r.put("isVip", 0);
		} else {
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
		return r;
	}

	@Override
	public boolean del(String taskId) {
		return updateTaskType(taskId, Task.TaskType.DEL.ordinal());
	}

	@Override
	public Row getTaskCount(String userId) {
		List<Task> alist = taskDao.getAcceptList(userId);
		List<Task> plist = taskDao.getList(userId);

		Row row = new Row();
		row.put("publishTaskCount", plist != null ? plist.size() : 0);
		row.put("acceptTaskCount", alist != null ? alist.size() : 0);
		return row;
	}

	@Override
	public TaskAccept getAccept(String taskId) {// 一个任务只能有一个抢到者
		log.info("taskId:" + taskId);
		return taskAcceptDao.findByTaskId(taskId);
	}

	@Override
	public Task getAccept(String userId, String taskId) {
		log.info("---userId:" + userId + ", taskId:" + taskId);
		return taskDao.getAccept(userId, taskId);
	}

	@Override
	public boolean delAccept(String userId, String taskId) {
		log.info("---userId:" + userId + ", taskId:" + taskId);
		TaskAccept one = taskAcceptDao.findByTaskIdAndCreator(taskId, userId);
		if (one != null) {
			one.setType(Task.TaskType.DEL.ordinal() + "");
			one.setUpdated(new Date());
			return taskAcceptDao.save(one) != null;
		}
		return false;
	}
//
//	private boolean pushMessageToUser(String content, String toUserId) {
//		String url = connectcenterUrl + "/message/sendSystemMsg";
//		Row params = new Row();
//		params.put("toAccount", new String[] { toUserId });
//
//		Row msgBody = new Row();
//		msgBody.put("type", "TIMTextElem");
//		msgBody.put("text", content);
//
//		params.put("msgBody", new Row[] { msgBody });
//
//		Result result = HttpUtil.doPostBody(url, params, "utf-8", Result.class);
//		if (result == null || result.getCode() != 0) {
//			log.error("send group msg fail", JSONObject.toJSONString(result));
//		}
//		if (result != null) {
//			log.info("push rlt:" + JSON.toJSONString(result));
//		}
//		return true;
//	}

	@Override
	public int getRobCount(String userId, String streamId) {
		int count = 0;
		if (StringUtils.isEmpty(streamId)) {
			count = taskDao.count(userId, TaskStatus.PUBLIC.ordinal());
		} else {
			count = taskDao.countByStream(userId, streamId, TaskStatus.PUBLIC.ordinal());
		}
		return count;
	}

	@Override
	public int getPublishTaskCount(String userId) {
		List<Task> plist = taskDao.getList(userId);
		return plist != null ? plist.size() : 0;
	}

	@Override
	public int getAcceptTaskCount(String userId) {
		List<Task> alist = taskDao.getAcceptList(userId);
		return alist != null ? alist.size() : 0;
	}
}
