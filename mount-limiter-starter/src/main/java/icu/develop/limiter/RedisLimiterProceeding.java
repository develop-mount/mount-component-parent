package icu.develop.limiter;

import icu.develop.limiter.exception.LimiterException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/4/26 10:30
 */
@Slf4j
public class RedisLimiterProceeding {
    /**
     * redisson client
     */
    private final RedissonClient redissonClient;

    public RedisLimiterProceeding(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 限制执行
     *
     * @param limiterKey     限流器名称
     * @param timeout 限制的一段时间，超时时间
     * @param count     限制的数量
     * @param proceeding     执行方法逻辑
     * @param <T>            泛型
     * @return 返回执行后的对象
     * @throws Throwable 异常
     */
    public <T> T limiter(String limiterKey, long timeout, long count, LimiterProceeding<T> proceeding) throws Throwable {

        // 获取限流器
        RRateLimiter rateLimiter = getRateLimiter(limiterKey, count, timeout);
        boolean limitSuccess = rateLimiter.tryAcquire();
        if (limitSuccess) {
            // 在限制内，执行方法逻辑
            return proceeding.proceed();
        } else {
            throw new LimiterException("超过限定速率,不能频繁操作,请稍后重试");
        }
    }

    /**
     * 获取限流器， 一秒一个，rate=1，rateInterval=1
     *
     * @param limiterKey     限流器名称
     * @param internalRate          令牌数量
     * @param internalRateInterval 一段时间 单位秒
     * @return 限流器
     */
    private RRateLimiter getRateLimiter(String limiterKey, long internalRate, long internalRateInterval) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(limiterKey);
        // 如果限流器不存在，就创建一个RRateLimiter限流器
        if (!rateLimiter.isExists()) {
            // 设置速率，每intervalSecond秒，参数count个令牌
            rateLimiter.trySetRate(RateType.OVERALL, internalRate, internalRateInterval, RateIntervalUnit.SECONDS);
        }

        // 获取限流的配置信息
        RateLimiterConfig rateLimiterConfig = rateLimiter.getConfig();
        // 上次配置的限流时间毫秒值
        Long rateInterval = rateLimiterConfig.getRateInterval();
        // 上次配置的限流次数
        Long rate = rateLimiterConfig.getRate();
        // 将timeOut转换成毫秒之后再跟rateInterval进行比较
        if (TimeUnit.MILLISECONDS.convert(internalRateInterval, TimeUnit.SECONDS) != rateInterval || internalRate != rate) {
            // 如果rateLimiterConfig的配置跟我们注解上面的值不一致，说明服务器重启过，程序员又修改了限流的配置
            // 删除原有配置
            rateLimiter.delete();
            // 以程序员重启后的限流配置为准，重新设置
            // 设置速率，每intervalSecond秒，参数count个令牌
            rateLimiter.trySetRate(RateType.OVERALL, internalRate, internalRateInterval, RateIntervalUnit.SECONDS);
        }

        return rateLimiter;
    }


}
