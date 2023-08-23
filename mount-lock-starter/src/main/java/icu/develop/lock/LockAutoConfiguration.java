package icu.develop.lock;

import icu.develop.lock.aspect.RedisLockAspect;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

/**
 * @author ：jwl
 * @version ：1.0
 * @description ： Redisson 配置
 * @date ：2022/8/22 20:25
 */
@Configuration
@Import({RedisAutoConfiguration.class, AopAutoConfiguration.class})
public class LockAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean(Config.class)
    public Config redissonConfig(RedisProperties redisProperties) {
        Config config = new Config();
        if (redisProperties.getSentinel() != null) {
            RedisProperties.Sentinel sentinel = redisProperties.getSentinel();
            SentinelServersConfig baseConfig = config.useSentinelServers();
            config.useSentinelServers().setDatabase(redisProperties.getDatabase());
            config.useSentinelServers().setMasterName(sentinel.getMaster());
            sentinel.getNodes().forEach(node -> config.useSentinelServers().addSentinelAddress("redis://" + node));
            config.useSentinelServers().setDatabase(redisProperties.getDatabase());
            if (StringUtils.hasLength(redisProperties.getPassword())) {
                baseConfig.setPassword(redisProperties.getPassword());
            }
        } else if (redisProperties.getCluster() != null) {
            ClusterServersConfig baseConfig = config.useClusterServers();
            redisProperties.getCluster().getNodes().forEach(node -> config.useClusterServers().addNodeAddress("redis://" + node));
            if (StringUtils.hasLength(redisProperties.getPassword())) {
                baseConfig.setPassword(redisProperties.getPassword());
            }
        } else if (StringUtils.hasLength(redisProperties.getHost())) {
            SingleServerConfig serverConfig = config.useSingleServer();
            serverConfig.setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort());
            serverConfig.setDatabase(redisProperties.getDatabase());
            if (StringUtils.hasLength(redisProperties.getPassword())) {
                serverConfig.setPassword(redisProperties.getPassword());
            }
        } else {
            throw new RuntimeException("没有找到Redis配置信息！");
        }
        config.useSingleServer().setConnectionMinimumIdleSize(10);
        config.setCodec(new JsonJacksonCodec());
        return config;
    }

    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(Config redissonConfig) {

        return Redisson.create(redissonConfig);
    }

    @Bean
    public RedisLockProceeding redisLockProceeding(RedissonClient redissonClient) {
        return new RedisLockProceeding(redissonClient);
    }

    @Bean
    public RedisLockAspect redisLockAspect(RedisLockProceeding redisLockProceeding) {
        return new RedisLockAspect(redisLockProceeding);
    }

}

