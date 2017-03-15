package com.feedss.content.task;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.feedss.content.entity.Category;
import com.feedss.content.entity.Category.CategoryType;
import com.feedss.content.entity.Stream.StreamStatus;
import com.feedss.content.service.CategoryService;
import com.feedss.content.service.StreamService;

/**
 * 更新分类下统计直播间和回放数量
 * @author zhouyujuan
 *
 */
@Component
@EnableScheduling
public class CategoryStreamTask {

	Log logger = LogFactory.getLog(getClass());
	
	@Resource
	private StreamService streamService;
	
	@Resource
	private CategoryService categoryService;
	
	@Scheduled(cron="0 0/5 * * * ?") //每5分钟执行一次
	public void work(){
		logger.debug("更新分类下统计直播间和回放数量....");
		List<Category> list = categoryService.selectCategoryList(CategoryType.ALL.ordinal());
		if(list==null || list.isEmpty()){
			return;
		}
		for(Category c:list){
			String categoryId = c.getUuid();
			int publishTotalCount = streamService.selectStreamCount(categoryId, StreamStatus.Publishing.ordinal()).intValue();
			int endedTotalCount = streamService.selectStreamCount(categoryId, StreamStatus.Ended.ordinal()).intValue();
			categoryService.updateStreamCount(publishTotalCount, endedTotalCount, categoryId);
		}
		
	}
}
