package icu.develop.limiter.aspect;

import icu.develop.limiter.RedisLimiterProceeding;
import icu.develop.limiter.annotation.RedisRateLimiter;
import icu.develop.limiter.constant.LimiterConstant;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/4/26 10:28
 */
@Slf4j
@Aspect
public class RedisRateLimiterAspect {
    private final RedisLimiterProceeding redisLimiterProceeding;

    public RedisRateLimiterAspect(RedisLimiterProceeding redisLimiterProceeding) {
        this.redisLimiterProceeding = redisLimiterProceeding;
    }

    /**
     * aop 限流切面
     *
     * @param redisRateLimiter redis限流注解
     */
    @Pointcut("@annotation(redisRateLimiter)")
    public void pointcut(RedisRateLimiter redisRateLimiter) {
    }

    /**
     * aop 执行
     *
     * @param joinPoint        {@link ProceedingJoinPoint}
     * @param redisRateLimiter {@link RedisRateLimiter}
     * @return 执行后的对象
     * @throws Throwable
     */
    @Around("pointcut(redisRateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint, RedisRateLimiter redisRateLimiter) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        String className = method.getDeclaringClass().getName();

        String limitKey;
        if (StringUtils.hasText(redisRateLimiter.limiterKey())) {
            limitKey = LimiterConstant.REDIS_KEY_LIMIT + redisRateLimiter.limiterKey();
        } else {
            limitKey = LimiterConstant.REDIS_KEY_LIMIT + className + ":" + methodName;
        }

        return redisLimiterProceeding.limiter(limitKey, redisRateLimiter.timeout(), redisRateLimiter.limitCount(), joinPoint::proceed);
    }
}
