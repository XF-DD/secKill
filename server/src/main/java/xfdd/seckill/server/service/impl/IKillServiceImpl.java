package xfdd.seckill.server.service.impl;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.joda.time.DateTime;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import xfdd.seckill.api.enums.SysConstant;
import xfdd.seckill.model.entity.ItemKill;
import xfdd.seckill.model.entity.ItemKillSuccess;
import xfdd.seckill.model.mapper.ItemKillMapper;
import xfdd.seckill.model.mapper.ItemKillSuccessMapper;
import xfdd.seckill.server.service.IKillService;
import xfdd.seckill.server.service.RabbitSenderService;
import xfdd.seckill.server.utils.RandomUtil;
import xfdd.seckill.server.utils.SnowFlake;

import java.util.concurrent.TimeUnit;

/**
 * @Author: XF-DD
 * @Date: 20/05/19 17:54
 */
@Service
public class IKillServiceImpl implements IKillService {

    @Autowired
    private ItemKillMapper itemKillMapper;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private RabbitSenderService rabbitSenderService;

    private SnowFlake snowFlake = new SnowFlake(3,5);

    /**
     * 商品秒杀核心业务逻辑的处理
     * @param killId
     * @param userId
     * @return
     */
    @Override
    public boolean killItem(int killId, int userId) throws Exception {
        boolean result = false;

        //判断当前用户是否已经抢购过当前商品
        if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
            // 查询待秒杀商品详情
            ItemKill itemKill = itemKillMapper.selectById(killId);

            //判断是否可以秒杀canKill = 1?
            if(itemKill!=null && itemKill.getCanKill()==1){
                //扣减库存-1
                int res = itemKillMapper.updateKillItem(killId);

                //扣减是否成功？-生成秒杀成功的订单，同时通知用户秒杀成功的消息
                if(res>0){
                    commonRecordKillSuccessInfo(itemKill,userId);
                    result = true;
                }
            }
        }else {
            throw new Exception("您已经买过啦！");
        }
        return result;
    }

    private void commonRecordKillSuccessInfo(ItemKill kill,Integer userId) throws Exception{
        //记录抢购成功后生成的秒杀订单记录

        ItemKillSuccess entity = new ItemKillSuccess();
        String orderNo = String.valueOf(snowFlake.nextId());

        entity.setCode(orderNo);
        entity.setItemId(kill.getItemId());
        entity.setKillId(kill.getId());
        entity.setUserId(userId.toString());
        entity.setStatus(SysConstant.SuccessButNotPay.getCode().byteValue());
        entity.setCreateTime(DateTime.now().toDate());
        //双重判断
        if(itemKillSuccessMapper.countByKillUserId(kill.getId(),userId)<=0){
            int res = itemKillSuccessMapper.insertSelective(entity);
            if(res>0){
                //异步邮件消息的通知： rabbitmq + mail
                rabbitSenderService.sendKillSuccessEmailMsg(orderNo);

                //入死信队列，用于失效超过指定的TTL时间时仍未支付的订单
                rabbitSenderService.sendKillSuccessOrderExpireMsg(orderNo);
            }
        }
    }

    //针对数据库的优化
    @Override
    public boolean killItemV2(int killId, int userId) throws Exception {
        boolean result = false;

        //判断当前用户是否已经抢购过当前商品
        if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
            // 查询待秒杀商品详情
            // 优化1：查询最后加上where total > 0
            ItemKill itemKill = itemKillMapper.selectByIdV2(killId);

            //判断是否可以秒杀canKill = 1?
            if(itemKill!=null && itemKill.getCanKill()==1){
                //扣减库存-1
                //优化2：查询最后加上where total > 0
                int res = itemKillMapper.updateKillItemV2(killId);

                //扣减是否成功？-生成秒杀成功的订单，同时通知用户秒杀成功的消息
                if(res>0){
                    commonRecordKillSuccessInfo(itemKill,userId);
                    result = true;
                }
            }
        }else {
            throw new Exception("您已经买过啦！");
        }
        return result;
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //redis的分布式锁
    @Override
    public boolean killItemV3(int killId, int userId) throws Exception {
        boolean result = false;

        if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
            //TODO: 借助redis的原子操作实现分布式锁-对公箱操作-共享资源进行操作
            ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
            final String key = new StringBuffer().append(killId).append(userId).append("-RedisLock").toString();
            final String value = RandomUtil.generateOrderCode();
            //用唯一key作为锁对象，由于redis单线程，只有一个线程返回true
            Boolean cacheRes = valueOperations.setIfAbsent(key, value);
            if(cacheRes){
                //设置过期时间，防止该用户死锁
                stringRedisTemplate.expire(key,30, TimeUnit.SECONDS);
                try{
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    if(itemKill!=null && itemKill.getCanKill()==1){
                        int res = itemKillMapper.updateKillItemV2(killId);
                        if(res>0){
                            commonRecordKillSuccessInfo(itemKill,userId);
                            result = true;
                        }
                    }
                }finally {
                    if(value.equals(valueOperations.get(key))){
                       stringRedisTemplate.delete(key);
                    }
                }
            }
        }else {
            throw new Exception("您已经买过啦！");
        }
        return result;
    }

    @Autowired
    private RedissonClient redissonClient;

    //redisson 的分布式锁
    @Override
    public boolean killItemV4(int killId, int userId) throws Exception {
        boolean result = false;

        final String lockKey = new StringBuffer().append(killId).append(userId).append("-RedissonLock").toString();
        RLock lock = redissonClient.getLock(lockKey);


        try{
//            lock.lock(10,TimeUnit.SECONDS);
            boolean cacheRes = lock.tryLock(30, 10, TimeUnit.SECONDS);
            if(cacheRes){
                //TODO 核心业务逻辑的处理
                if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    if(itemKill!=null && itemKill.getCanKill()==1){
                        int res = itemKillMapper.updateKillItemV2(killId);
                        if(res>0){
                            commonRecordKillSuccessInfo(itemKill,userId);
                            result = true;
                        }
                    }
                }else {
                    throw new Exception("您已经买过啦！");
                }
            }
        }finally {
            lock.unlock();
            //强制释放
//            lock.forceUnlock();
        }
        return result;
    }


    @Autowired
    private CuratorFramework curatorFramework;

    private static final String pathPrefix = "/kill/zkLock/";

    //基于ZooKeeper的分布式锁
    @Override
    public boolean killItemV5(int killId, int userId) throws Exception {
        boolean result = false;

        //TODO 同个商品同个人若多次请求会创建同个路径下多个有序节点
        //public InterProcessMutex(CuratorFramework client, String path)
        InterProcessMutex mutex = new InterProcessMutex(curatorFramework,pathPrefix+killId+userId+"-lock");
        try{
            //尝试获取锁，10秒 TODO 只有第一个节点(序号最小的)能得到锁
            if (mutex.acquire(10L,TimeUnit.SECONDS)){
                if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    if(itemKill!=null && itemKill.getCanKill()==1){
                        int res = itemKillMapper.updateKillItemV2(killId);
                        if(res>0){
                            commonRecordKillSuccessInfo(itemKill,userId);
                            result = true;
                        }
                    }
                }else {
                    throw new Exception("zookeeper~您已经抢购过该商品了！");
                }
            }
        }catch(Exception e){
            throw new Exception("不在抢购时间内或已被抢购完毕！");
        }finally {
            if(mutex!=null){
                //释放后下一个序号得到锁
                mutex.release();
            }
        }
        return result;
    }
}
