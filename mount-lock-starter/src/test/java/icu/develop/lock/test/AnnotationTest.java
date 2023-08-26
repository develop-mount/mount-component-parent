package icu.develop.lock.test;

import icu.develop.lock.RedisLockProceeding;
import icu.develop.lock.annotation.RedisLock;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/26 12:15
 */
public class AnnotationTest {

    @RedisLock(lockKey = "testKey", waitTime = 2, lockedTime = 10)
    void testAnnotation() {

    }


    @Resource
    RedisLockProceeding redisLockProceeding;

    void testLock() {
        long waitTime = 2;
        long leaseTime = 20;
        try {
            Object lockKey = redisLockProceeding.locked("lockKey", waitTime, leaseTime, () -> {
                // 执行业务逻辑代码
                return null;
            });
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
