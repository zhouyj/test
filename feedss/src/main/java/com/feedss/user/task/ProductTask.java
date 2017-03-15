package com.feedss.user.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.feedss.user.service.UsrProductService;

/**
 * Created by qin.qiang on 2016/9/5 0005.
 */
@Component
@EnableScheduling
public class ProductTask {

    @Autowired
    private UsrProductService productService;

    private static final Logger logger = LoggerFactory.getLogger(ProductTask.class);

    public static boolean canExcute = true;

    @Scheduled(cron = "0 * * * * ?") //每1分钟执行一次
    public void scheduler(){
        if(!canExcute){ //如果定时之前的没执行完,直接返回
            return;
        }
        try {
            canExcute = false ; //开始执行,设置为false
            logger.info("******************Receive Gift Start*********************");
            productService.receiveGift();
            logger.info("******************Receive Gift End*********************");
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            canExcute = true ;
        }
    }
}
