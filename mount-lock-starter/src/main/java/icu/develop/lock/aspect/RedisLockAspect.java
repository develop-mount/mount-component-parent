package icu.develop.lock.aspect;

import icu.develop.lock.RedisLockProceeding;
import icu.develop.lock.annotation.RedisLock;
import icu.develop.lock.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author ：linfeng
 * description ：redis分布式锁 AOP
 * program ：bmp-prm
 * @since ：Created in 2022/8/30 10:17
 */
@Slf4j
@Aspect
public class RedisLockAspect {

    private final RedisLockProceeding redisLockProceeding;

    public RedisLockAspect(RedisLockProceeding redisLockProceeding) {
        this.redisLockProceeding = redisLockProceeding;
    }

    /**
     * aop lock切面
     *
     * @param redisLock redis锁注解
     */
    @Pointcut("@annotation(redisLock)")
    public void pointcut(RedisLock redisLock) {
    }

    /**
     * aop 执行防范
     *
     * @param joinPoint {@link ProceedingJoinPoint}
     * @param redisLock {@link RedisLock}
     * @return 执行后的对象
     * @throws Throwable 异常
     */
    @Around("pointcut(redisLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        String className = method.getDeclaringClass().getName();

        String lockKey;
        if (StringUtils.hasText(redisLock.lockKey())) {
            lockKey = RedisConstant.REDIS_KEY_PRM_LOCK + redisLock.lockKey();
        } else {
            lockKey = RedisConstant.REDIS_KEY_PRM_LOCK + className + ":" + methodName;
        }

        return redisLockProceeding.locked(lockKey, redisLock.waitTime(), redisLock.lockedTime(), joinPoint::proceed);
    }
}
