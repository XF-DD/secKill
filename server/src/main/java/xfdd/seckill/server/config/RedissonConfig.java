package xfdd.seckill.server.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @Author: XF-DD
 * @Date: 20/05/23 15:59
 */
@Configuration
public class RedissonConfig {

    @Autowired
    private Environment environment;

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer()
                .setAddress(environment.getProperty("redis.config.host"))
                .setPassword(environment.getProperty("redis.config.password"));
        return Redisson.create(config);
    }
}
