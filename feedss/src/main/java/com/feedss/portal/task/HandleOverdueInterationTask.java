package com.feedss.portal.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.feedss.portal.service.InteractionService;

/**
 * 清理逾期未处理的任务及投标，有效期7天，超过有效期未处理的interaction将被处理，退还虚拟币
 * @author zhouyujuan
 *
 */
@Component
@EnableScheduling
public class HandleOverdueInterationTask {

    @Autowired
    private InteractionService interactionService;
    

    private static final Logger logger = LoggerFactory.getLogger(HandleOverdueInterationTask.class);

    public static boolean canExcute = true;

    @Scheduled(cron = "0 0 * * * ?")//每小时执行一次
    public void scheduler(){
        try {
            logger.info("******************handle overdue refresh start*********************");
            interactionService.handleOverdue();
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
