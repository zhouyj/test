package com.feedss.portal.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonData;
import com.feedss.base.Row;
import com.feedss.base.util.CommonUtil;
import com.feedss.contact.entity.Message;
import com.feedss.contact.service.MessageService;
import com.feedss.portal.common.ErrorConstants;
import com.feedss.portal.controller.domain.TaskParam;
import com.feedss.portal.dto.TaskDTO;
import com.feedss.portal.entity.Task;
import com.feedss.portal.entity.Task.TaskStatus;
import com.feedss.portal.service.TaskService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/task")
public class TaskController extends BaseController {

    @Resource
    private TaskService taskService;
    @Resource
    private MessageService messageService;

    @ResponseBody
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test(HttpServletRequest request) {
        return "Hello World!";
    }

    @ResponseBody
    @RequestMapping(value = "/publish", method = RequestMethod.POST)
    public JsonData publish(HttpServletRequest request, @RequestBody String msgStr) {
        TaskParam param = JSON.parseObject(msgStr, TaskParam.class);
        if (param.getDescription().length() < 1) {
            return JsonData.fail(ErrorCode.INVALID_PARAMETERS, "任务描述太短");
        }

        Task one = taskService.add(getUserIdFromHeader(request), param.getDescription(), param.getStreamId());
        if (one == null) {
            return JsonData.fail(ErrorCode.ERROR, ErrorConstants.COMMON_ERROR_MSG);
        }
        // 发一个消息到直播间
        if (StringUtils.isNoneBlank(param.getStreamId())) {
        	String groupId = CommonUtil.getGroupId(param.getStreamId());
        	
        	Map<String, Object> ext = new HashMap<String, Object>();
        	ext.put("messageSource", Message.Source.TaskRequest.name());
        	ext.put("task", one);
        	ext.put("groupId", groupId);
        	messageService.sendGroupMessage(groupId, null, ext, null);
        }
        JsonData r = new JsonData(ErrorCode.SUCCESS);
        r.addData("task", one);
        return r;
    }

    @ResponseBody
    @RequestMapping(value = "/rob", method = RequestMethod.POST)
    public JsonData rob(HttpServletRequest request, @RequestBody String paramStr) {
        TaskParam param = JSON.parseObject(paramStr, TaskParam.class);
        String userId = getUserIdFromHeader(request);
        JsonData flag = taskService.rob(userId, param.getTaskId());
        if (flag.getCode() != 0) {
            return JsonData.fail(ErrorCode.INVALID_PARAMETERS, flag.getMsg());
        }
     // 发一个消息到直播间
        if (StringUtils.isNoneBlank(param.getStreamId())) {
        	String text = "认领了任务";
        	Row user = taskService.getUser(userId);
        	//creator & stream
        	List<TaskDTO> list = taskService.getPublishTaskList(param.getStreamId(), TaskStatus.PUBLIC.ordinal());
            int isRob = 0;
            if(list!=null && !list.isEmpty()){
            	isRob = 1;
            }
            
            String groupId = CommonUtil.getGroupId(param.getStreamId());
        	
        	Map<String, Object> ext = new HashMap<String, Object>();
        	ext.put("messageSource", Message.Source.TaskResponse.name());
        	ext.put("text", text);
        	ext.put("userInfo", user);
        	ext.put("isRob", isRob);
        	ext.put("groupId", groupId);
        	messageService.sendGroupMessage(groupId, null, ext, null);
        }
        return JsonData.success("成功抢到任务");
    }

    @ResponseBody
    @RequestMapping(value = "/applyFinish", method = RequestMethod.POST)
    public JsonData applyFinish(HttpServletRequest request, @RequestBody String paramStr) {
        TaskParam param = JSON.parseObject(paramStr, TaskParam.class);
        boolean flag = taskService.applyFinish(getUserIdFromHeader(request), param.getTaskId());
        if (!flag) {
            return JsonData.fail(ErrorCode.INTERNAL_ERROR, "操作异常");
        }

        return JsonData.success("申请完成成功");
    }

    @ResponseBody
    @RequestMapping(value = "/confirmFinish", method = RequestMethod.POST)
    public JsonData confirmFinish(HttpServletRequest request, @RequestBody String paramStr) {
        TaskParam param = JSON.parseObject(paramStr, TaskParam.class);
        boolean flag = taskService.confirmFinish(getUserIdFromHeader(request), param.getTaskId());
        if (!flag) {
            return JsonData.fail(ErrorCode.INTERNAL_ERROR, "操作异常");
        }

        return JsonData.success("确认完成成功");
    }

    @ResponseBody
    @RequestMapping(value = "/reject", method = RequestMethod.POST)
    public JsonData reject(HttpServletRequest request, @RequestBody String paramStr) {
        TaskParam param = JSON.parseObject(paramStr, TaskParam.class);
        boolean flag = taskService.reject(getUserIdFromHeader(request), param.getTaskId(), param.getDescription());
        if (!flag) {
            return JsonData.fail(ErrorCode.INTERNAL_ERROR, "操作异常");
        }

        return JsonData.success("驳回成功");
    }

    @ResponseBody
    @RequestMapping(value = "/del", method = RequestMethod.POST)
    public JsonData del(HttpServletRequest request, @RequestBody String paramStr) {
        log.debug("delTask, " + paramStr);
        try {
            TaskParam param = JSON.parseObject(paramStr, TaskParam.class);
            Task one = taskService.get(param.getTaskId());
            if (one == null) {
                return JsonData.fail(ErrorCode.INTERNAL_ERROR, "任务不存在");
            }

            if(!getUserIdFromHeader(request).equals(one.getCreator())){
                return JsonData.fail(ErrorCode.BALANCE_NOT_ENOUGH, "只能删自己发布的任务");
            }

            if (one.getStatus() != Task.TaskStatus.PUBLIC.ordinal()
                    && one.getStatus() != Task.TaskStatus.FINISH.ordinal()) {
                return JsonData.fail(ErrorCode.HAVE_HANDLED, "只有未抢和已完成的任务才能删除");
            }

            taskService.del(param.getTaskId());
            return JsonData.success("删除成功");
        } catch (Exception e) {
            log.error("delTsk error", e);
        }
        return JsonData.fail(ErrorCode.INTERNAL_ERROR, "操作异常");
    }

    @ResponseBody
    @RequestMapping(value = "/delAccept", method = RequestMethod.POST)
    public JsonData delAccept(HttpServletRequest request, @RequestBody String paramStr) {
        log.debug("delTask, " + paramStr);
        try {
            TaskParam param = JSON.parseObject(paramStr, TaskParam.class);
            Task one = taskService.get(param.getTaskId());
            if (one == null) {
                return JsonData.fail(ErrorCode.INTERNAL_ERROR, "任务不存在");
            }

            Task task = taskService.getAccept(getUserIdFromHeader(request), param.getTaskId());
            if(task == null){
                return JsonData.fail(ErrorCode.INTERNAL_ERROR, "任务不存在");
            }

            if (one.getStatus() != Task.TaskStatus.FINISH.ordinal()) {
                return JsonData.fail(ErrorCode.HAVE_HANDLED, "只有已完成的任务才能删除");
            }

            taskService.delAccept(getUserIdFromHeader(request), param.getTaskId());
            return JsonData.success("删除成功");
        } catch (Exception e) {
            log.error("delTsk error", e);
        }
        return JsonData.fail(ErrorCode.INTERNAL_ERROR, "操作异常");
    }

    @ResponseBody
    @RequestMapping(value = "/listByStream", method = RequestMethod.POST)
    public JsonData listByStream(HttpServletRequest request, @RequestBody String msgStr) {
        TaskParam param = JSON.parseObject(msgStr, TaskParam.class);
        List<TaskDTO> tmpList = taskService.getPublishTaskList(param.getUserId(), param.getStreamId());
        List<TaskDTO> list = new ArrayList<TaskDTO>();

        JsonData r = JsonData.success("ok");
        if (StringUtils.isNoneBlank(param.getTaskStatus())) {
            int status = -1;
            try {
                status = Integer.parseInt(param.getTaskStatus());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            for (TaskDTO dto : tmpList) {
                if (dto.getStatus() == status) {
                    list.add(dto);
                }
            }
            r.addData("list", list);
        } else {
            r.addData("list", tmpList);
        }


        return r;
    }
}
