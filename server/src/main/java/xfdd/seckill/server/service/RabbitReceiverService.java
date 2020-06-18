package xfdd.seckill.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import xfdd.seckill.model.dto.KillSuccessUserInfo;
import xfdd.seckill.model.entity.ItemKill;
import xfdd.seckill.model.entity.ItemKillSuccess;
import xfdd.seckill.model.mapper.ItemKillSuccessMapper;
import xfdd.seckill.server.dto.MailDto;

/**
 * RabbitMQ接收消息服务
 * @Author: XF-DD
 * @Date: 20/05/20 10:59
 */
@Service
public class RabbitReceiverService {
    public static final Logger logger = LoggerFactory.getLogger(RabbitReceiverService.class);

    @Autowired
    private MailService mailService;

    @Autowired
    private Environment environment;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    /**
     * 秒杀异步邮件通知-接收消息
     */
    @RabbitListener(queues = {"${mq.kill.item.success.email.queue}"},containerFactory = "singleListenerContainer")
    public void consumeEmailMsg(KillSuccessUserInfo info){
        try{
            logger.info("秒杀异步邮件通知-接收消息：{}",info);
            //%s,%s ItemName,Code
            String content = String.format(environment.getProperty("mail.kill.item.success.content"), info.getItemName(), info.getCode());
            //发送邮件
            MailDto dto = new MailDto(environment.getProperty("mail.kill.item.success.subject"), content, new String[]{(info.getEmail())});
            mailService.sendHTMLMail(dto);
        }catch(Exception e){
            logger.info("秒杀异步邮件通知-发生异常：",e.fillInStackTrace());
        }
    }

    /**
     * 用户秒杀成功后超时未支付-监听者
     * 监听真正队列
     */
    @RabbitListener(queues = {"${mq.kill.item.success.kill.dead.real.queue}"},containerFactory = "singleListenerContainer")
    public void consumeExpireOrder(KillSuccessUserInfo info){
        try{
            logger.info("用户秒杀成功后超时未支付-监听者-接收消息：{}",info);
            if(info!=null){
                ItemKillSuccess entity = itemKillSuccessMapper.selectByPrimaryKey(info.getCode());
                if(entity!=null && entity.getStatus().intValue() == 0){
                    //将status由0变为-1，即由成功未支付变为失效
                    itemKillSuccessMapper.expireOrder(info.getCode());
                }
            }
        }catch(Exception e){
            logger.info("用户秒杀成功后超时未支付-监听者-发生异常：",e.fillInStackTrace());
        }
    }
}
