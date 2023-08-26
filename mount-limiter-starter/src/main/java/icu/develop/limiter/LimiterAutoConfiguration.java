package icu.develop.limiter;

import icu.develop.limiter.aspect.RedisRateLimiterAspect;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

/**
 * @author ：jwl
 * @version ：1.0
 * @since  ：2022/8/22 20:25
 */
@Configuration
@Import({RedisAutoConfiguration.class, AopAutoConfiguration.class})
public class LimiterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Config.class)
    public Config redissonConfig(@Autowired RedisProperties redisProperties) {
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
    public RedisLimiterProceeding redisLimiterProceeding(RedissonClient redissonClient) {
        return new RedisLimiterProceeding(redissonClient);
    }

    @Bean
    public RedisRateLimiterAspect redisRateLimiterAspect(RedisLimiterProceeding redisLimiterProceeding) {
        return new RedisRateLimiterAspect(redisLimiterProceeding);
    }

}

