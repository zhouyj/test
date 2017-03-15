package com.feedss.manage.service;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonData;
import com.feedss.user.model.UserVo;
import com.feedss.user.service.ApplyService;
/**
 * 审核
 * @author 张杰
 *
 */
@Service
public class ManageApplyService {

	@Autowired
	ApplyService applyService;
	
	public JSONObject applyPass(UserVo userVo,String applyId){
		int row=applyService.reply(applyId, 1);
		if(row>0){
			JSONObject result = new JSONObject();
			result.put("code", ErrorCode.SUCCESS.ordinal());
			return result;
		}else{
			JSONObject result = new JSONObject();
			result.put("code", ErrorCode.ERROR.getCode());
			result.put("msg",  ErrorCode.ERROR.getMsg());
			return result;
		}
	}
	
	public Map<String, Object> pageFindApply(String uuid,String nickname,String mobile,
			String field,
			String direction,int page,int size,UserVo user){
		Map<String, Object> map = applyService.getApplyVList(uuid, nickname, mobile, field, direction, page, size);
		
		if(map==null){
			return null;
		}
		long totalCount=(long) map.get("totalCount");
		int pageSize=(int) map.get("pageSize");
		map.put("pageNo", page);
		long totalPages=totalCount%pageSize==0?
				totalCount/pageSize:(totalCount/pageSize)+1;
		map.put("totalPages", totalPages);
		
		return map;
	}

	public JSONObject rejected(UserVo userVo,String applyId) {
		int row=applyService.reply(applyId, -1);
		if(row>0){
			JSONObject result = new JSONObject();
			result.put("code", ErrorCode.SUCCESS.getCode());
			return result;
		}else{
			JSONObject result = new JSONObject();
			result.put("code", ErrorCode.ERROR.getCode());
			result.put("msg", ErrorCode.ERROR.getMsg());
			return result;
		}
	}
	
}
