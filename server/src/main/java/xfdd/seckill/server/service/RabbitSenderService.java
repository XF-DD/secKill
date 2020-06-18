package xfdd.seckill.server.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import xfdd.seckill.model.dto.KillSuccessUserInfo;
import xfdd.seckill.model.mapper.ItemKillSuccessMapper;

import java.nio.charset.StandardCharsets;

/**
 * RabbitMQ发送消息服务
 * @Author: XF-DD
 * @Date: 20/05/20 10:58
 */
@Service
public class RabbitSenderService {

    public static final Logger logger = LoggerFactory.getLogger(RabbitReceiverService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    Environment environment;

    @Autowired
    ItemKillSuccessMapper itemKillSuccessMapper;

    /**
     * 秒杀成功异步发送邮件通知消息
     */
    public void sendKillSuccessEmailMsg(String orderNo){
        logger.info("秒杀成功异步发送邮件通知消息-准备发送消息：{}",orderNo);
        try{
            if(StringUtils.isNotBlank(orderNo)){
                KillSuccessUserInfo info = itemKillSuccessMapper.selectByCode(orderNo);
                if(info!=null){
                    //rabbitmq发送消息的逻辑
                    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                    rabbitTemplate.setExchange(environment.getProperty("mq.kill.item.success.email.exchange"));
                    rabbitTemplate.setRoutingKey(environment.getProperty("mq.kill.item.success.email.routing.key"));

                    //将info充当消息发送至队列,若没有则创建队列
                    rabbitTemplate.convertAndSend(info, new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            MessageProperties messageProperties = message.getMessageProperties();
                            //消息传送模式，持久
                            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            //消息头，指定确切类型，在接受者接收方法可以直接用KillSuccessUserInfo.class接收
                            //key-value
                            messageProperties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,KillSuccessUserInfo.class);
                            return message;
                        }
                    });
                }
            }
        }catch(Exception e){
            logger.error("秒杀成功异步发送邮件通知消息-发生异常，消息为：{}",orderNo,e.fillInStackTrace());
        }
    }

    /**
     * 秒杀成功后生成抢购订单-发送信息入死信队列，等待着一定时间失效超时未支付的订单
     * @param orderCode
     */
    public void sendKillSuccessOrderExpireMsg(final String orderCode){
        try{
            if(StringUtils.isNotBlank(orderCode)){
                KillSuccessUserInfo info = itemKillSuccessMapper.selectByCode(orderCode);
                if(info != null){
                    //发送消息的逻辑
                    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                    rabbitTemplate.setExchange(environment.getProperty("mq.kill.item.success.kill.dead.prod.exchange"));
                    rabbitTemplate.setRoutingKey(environment.getProperty("mq.kill.item.success.kill.dead.prod.key"));

                    //将info充当消息发送至队列,若没有则创建队列
                    rabbitTemplate.convertAndSend(info, new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            MessageProperties messageProperties = message.getMessageProperties();
                            //消息传送模式，持久
                            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            //消息头，指定确切类型，在接受者接收方法可以直接用KillSuccessUserInfo.class接收
                            messageProperties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,KillSuccessUserInfo.class);

                            //动态设置TTL
                            messageProperties.setExpiration(environment.getProperty("mq.kill.item.success.kill.expire"));
                            return message;
                        }
                    });
                }
            }
        }catch(Exception e){
            logger.error("秒杀成功后生成抢购订单-发送信息入死信队列-发生异常，消息为：{}",orderCode,e.fillInStackTrace());
        }
    }




}
