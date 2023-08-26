package icu.develop.l2cache;

import icu.develop.l2cache.interceptor.*;
import icu.develop.l2cache.listener.L2CacheKeyExpirationListener;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.Map;

import static icu.develop.l2cache.constant.L2CacheConstant.DEFAULT_CACHE_NAME;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/4 17:25
 */
@EnableAspectJAutoProxy
@Configuration
@Import({RedisAutoConfiguration.class, AopAutoConfiguration.class})
@EnableConfigurationProperties(L2CacheProperties.class)
public class L2CacheAutoConfiguration {

    /**
     * 不缓存空值策略
     *
     * @return 策略
     */
    @Bean
    public NotCacheNullCacheStrategy notCacheNull2CacheStrategy() {
        return new NotCacheNullCacheStrategy();
    }

    /**
     * 缓存空值策略
     *
     * @return 策略
     */
    @Bean
    public CacheNullCacheStrategy cacheNull2CacheStrategy() {
        return new CacheNullCacheStrategy();
    }

    /**
     * RedisMessageListenerContainer
     *
     * @param connectionFactory RedisConnectionFactory
     * @return container
     */
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    /**
     * 过期key处理
     *
     * @param redisMessageListenerContainer redis container
     * @return key过期监听器
     */
    @ConditionalOnMissingBean(L2CacheKeyExpirationListener.class)
    @Bean
    public L2CacheKeyExpirationListener l2KeyExpirationListener(RedisMessageListenerContainer redisMessageListenerContainer) {
        return new L2CacheKeyExpirationListener(redisMessageListenerContainer);
    }

    @Bean(name = {"l2CacheInterceptor"})
    public L2CacheInterceptor l2CacheInterceptor(StringRedisTemplate stringRedisTemplate, L2CacheProperties l2CacheProperties) {

        L2CacheManager.putCache(DEFAULT_CACHE_NAME,
                new DelegateL2Cache(new CaffeineL2Cache(new RedisL2Cache(DEFAULT_CACHE_NAME, stringRedisTemplate, l2CacheProperties.getTtl()), l2CacheProperties.getTtl())));

        for (Map.Entry<String, L2CacheProperties.CacheItem> itemEntry : l2CacheProperties.getItems().entrySet()) {
            if (!DEFAULT_CACHE_NAME.equalsIgnoreCase(itemEntry.getKey())) {
                L2CacheManager.putCache(itemEntry.getKey(),
                        new DelegateL2Cache(new CaffeineL2Cache(new RedisL2Cache(itemEntry.getKey(), stringRedisTemplate, itemEntry.getValue().getTtl()), itemEntry.getValue().getTtl())));
            }
        }
        L2CacheInterceptor interceptor = new L2CacheInterceptor();
        interceptor.configure(new AnnotationL2CacheOperationSource(), DefaultL2CacheResolver::new);
        return interceptor;
    }

    /**
     * aop
     *
     * @param l2CacheInterceptor 二级缓存拦截器
     * @return Advisor对象
     */
    @Bean
    public DefaultPointcutAdvisor defaultPointcutAdvisor(L2CacheInterceptor l2CacheInterceptor) {

        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("@annotation(com.vevor.prm.common.cache.annotation.L2Cacheable) or " +
                "@annotation(com.vevor.prm.common.cache.annotation.L2CacheEvict) or " +
                "@annotation(com.vevor.prm.common.cache.annotation.L2CachePut)");
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(pointcut);
        advisor.setAdvice(l2CacheInterceptor);
        return advisor;
    }

}
