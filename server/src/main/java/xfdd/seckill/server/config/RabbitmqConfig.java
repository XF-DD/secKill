package xfdd.seckill.server.config;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;

/**
 * @Author: XF-DD
 * @Date: 20/05/19 23:04
 * 通用化 Rabbitmq 的配置
 */
@Configuration
public class RabbitmqConfig {

    private final static Logger logger = LoggerFactory.getLogger(RabbitmqConfig.class);

    @Autowired
    private Environment environment;

    //建立通道连接，缓存连接工厂
    @Autowired
    private CachingConnectionFactory connectionFactory;

    //消费者所在容器工厂
    @Autowired
    private SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer;

    /**
     * 单一消费者
     *
     * @return
     */
    @Bean(name = "singleListenerContainer")
    public SimpleRabbitListenerContainerFactory listenerContainer() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter()); //消息传输格式
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setPrefetchCount(1);
        factory.setTxSize(1);
        return factory;
    }

    /**
     * 多个消费者
     *
     * @return
     */
    @Bean(name = "multiListenerContainer")
    public SimpleRabbitListenerContainerFactory multiListenerContainer() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factoryConfigurer.configure(factory, connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        //确认消费模式-NONE
        factory.setAcknowledgeMode(AcknowledgeMode.NONE);
        //多少个消费者
        factory.setConcurrentConsumers(environment.getProperty("spring.rabbitmq.listener.simple.concurrency", int.class));
        //最多多少个消费者
        factory.setMaxConcurrentConsumers(environment.getProperty("spring.rabbitmq.listener.simple.max-concurrency", int.class));
        //每个消费者预拉取数量
        factory.setPrefetchCount(environment.getProperty("spring.rabbitmq.listener.simple.prefetch", int.class));
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        //必须要设置,发送确认
        connectionFactory.setPublisherConfirms(true);
        connectionFactory.setPublisherReturns(true);

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * @param correlationData 唯一标识，有了这个唯一标识，我们就知道可以确认（失败）哪一条消息了
             * @param ack
             * @param cause
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                logger.info("消息发送成功:correlationData({}),ack({}),cause({})", correlationData, ack, cause);
            }
        });
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                logger.warn("消息丢失:exchange({}),route({}),replyCode({}),replyText({}),message:{}", exchange, routingKey, replyCode, replyText, message);
            }
        });
        return rabbitTemplate;
    }
    //构建异步发送邮箱通知的消息模型
    @Bean
    //public Queue(String name, boolean durable)
    public Queue successEmailQueue(){
        return new Queue(environment.getProperty("mq.kill.item.success.email.queue"),true);
    }

    @Bean
    public TopicExchange successEmailExchange(){
        return new TopicExchange(environment.getProperty("mq.kill.item.success.email.exchange"),true,false);
    }

    @Bean
    public Binding successEmailBinding(){
        return BindingBuilder.bind(successEmailQueue()).to(successEmailExchange()).with(environment.getProperty("mq.kill.item.success.email.routing.key"));
    }

    //构建秒杀成功之后-订单超时未支付的死信队列消息模型
    @Bean
    //public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
    public Queue successKillDeadQueue(){
        HashMap<String, Object> argsMap = Maps.newHashMap();
        //固定写法，这里设置消息死后发送到的交换机和路由key
        argsMap.put("x-dead-letter-exchange",environment.getProperty("mq.kill.item.success.kill.dead.exchange"));
        argsMap.put("x-dead-letter-routing-key",environment.getProperty("mq.kill.item.success.kill.dead.routing.key"));
        //设置TTL
//        argsMap.put("x-message-ttl",10000);
        return new Queue(environment.getProperty("mq.kill.item.success.kill.dead.queue"),true,false,false,argsMap);
    }

    //基本交换机
    @Bean
    public TopicExchange successKillDeadProdExchange(){
        return new TopicExchange(environment.getProperty("mq.kill.item.success.kill.dead.prod.exchange"),true,false);
    }

    //创建基本交换机 + 基本路由 -> 死信队列的绑定
    @Bean
    public Binding successKillDeadProdBinding(){
        return BindingBuilder.bind(successKillDeadQueue()).to(successKillDeadProdExchange()).with(environment.getProperty("mq.kill.item.success.kill.dead.prod.key"));
    }


    // 真正的队列
    @Bean
    public Queue successKillRealQueue(){
        return new Queue(environment.getProperty("mq.kill.item.success.kill.dead.real.queue"));
    }

    //死信交换机
    @Bean
    public TopicExchange successKillDeadExchange(){
        return new TopicExchange(environment.getProperty("mq.kill.item.success.kill.dead.exchange"));
    }

    //死信交换机+死信路由 ->真正队列的绑定
    @Bean
    public Binding successKillDeadBinding(){
        return BindingBuilder.bind(successKillRealQueue()).to(successKillDeadExchange()).with("mq.kill.item.success.kill.dead.routing.key");
    }

}
