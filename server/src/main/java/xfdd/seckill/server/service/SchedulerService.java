package xfdd.seckill.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xfdd.seckill.model.entity.ItemKillSuccess;
import xfdd.seckill.model.mapper.ItemKillSuccessMapper;

import java.util.List;

/**
 * @Author: XF-DD
 * @Date: 20/05/21 22:51
 */
@Service
public class SchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private Environment env;


    /**
     * 定时获取status=0的订单并判断是否超过TTL，然后进行失效
     */
//    @Scheduled(cron = "0/10 * * * * ?")
    @Scheduled(cron = "0 0/30 * * * ?")
    public void schedulerExpireOrders(){
        logger.info("v1的定时任务----");

        try {
            List<ItemKillSuccess> list=itemKillSuccessMapper.selectExpireOrders();
            if (list!=null && !list.isEmpty()){
   //             java8的写法
                list.stream().forEach(i -> {
                    if (i!=null && i.getDiffTime() > env.getProperty("scheduler.expire.orders.time",Integer.class)){
                        itemKillSuccessMapper.expireOrder(i.getCode());
                    }
                });
            }

            /*for (ItemKillSuccess entity:list){
            }*/ //非java8的写法
        }catch (Exception e){
            logger.error("定时获取status=0的订单并判断是否超过TTL，然后进行失效-发生异常：",e.fillInStackTrace());
        }
    }

//    @Scheduled(cron = "0/11 * * * * ?")
//    public void schedulerExpireOrdersV2(){
//        log.info("v2的定时任务----");
//    }
//
//    @Scheduled(cron = "0/10 * * * * ?")
//    public void schedulerExpireOrdersV3(){
//        log.info("v3的定时任务----");
//    }
}
