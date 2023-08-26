package icu.develop.limiter;

import icu.develop.limiter.annotation.RedisRateLimiter;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/26 12:32
 */
public class RedisRateLimiterTest {

    @RedisRateLimiter(limiterKey="limiterKey", timeout = 100, limitCount = 10)
    void testAnnotation() {

    }

    @Resource
    RedisLimiterProceeding redisLimiterProceeding;

    void testLock() {
        long timeout = 2;
        long count = 20;
        try {
            Object limiter = redisLimiterProceeding.limiter("limitKey", timeout, count, () -> {
                // 执行业务逻辑代码
                return null;
            });
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
