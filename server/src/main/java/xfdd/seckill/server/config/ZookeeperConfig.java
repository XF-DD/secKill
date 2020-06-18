package xfdd.seckill.server.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Zookeeper组件自定义
 * @Author: XF-DD
 * @Date: 20/05/23 17:22
 */
@Configuration
public class ZookeeperConfig {

    @Autowired
    private Environment environment;

    /**
     * 自定义注入ZooKeeper客户端
     */
    @Bean
    public CuratorFramework curatorFramework(){
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(environment.getProperty("zk.host"))
                .namespace(environment.getProperty("zk.namespace"))
                //重试策略,多种实现  5次，每次间隔1s
                .retryPolicy(new RetryNTimes(5, 1000))
                .build();
        curatorFramework.start();
        return curatorFramework;
    }
}
