package com.feedss.user.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.feedss.user.service.UserService;

/**
 * 主持人榜单，被关注数，5分钟刷一次
 * @author zhouyujuan
 *
 */
@Component
@EnableScheduling
public class HostListTask {

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(HostListTask.class);

    public static boolean canExcute = true;

    @Scheduled(cron = "5 * * * * ?") //每1分钟执行一次
    public void scheduler(){
        if(!canExcute){ //如果定时之前的没执行完,直接返回
            return;
        }
        try {
            canExcute = false ; //开始执行,设置为false
            logger.info("******************host list refresh start*********************");
            userService.refreshHostList();
            logger.info("******************host list refresh end*********************");
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            canExcute = true ;
        }
    }
}
