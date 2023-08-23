package icu.develop.lock;

import icu.develop.lock.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/3/30 18:04
 */
@Slf4j
public class RedisLockProceeding {

    /**
     * redisson client
     */
    private final RedissonClient redissonClient;

    public RedisLockProceeding(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Redis分布式锁
     *
     * @param lockKey    锁定Key
     * @param waitTime   等待时间 获取锁等待时间
     * @param leaseTime  等待时间后删除锁
     * @param proceeding 锁定后执行的方法
     * @param <T>        泛型
     * @return 执行后的对象
     */
    public <T> T locked(String lockKey, long waitTime, long leaseTime, LockProceeding<T> proceeding) throws Throwable {

        String finalLockKey = RedisConstant.REDIS_KEY_PRM_LOCK + lockKey;

        try {
            RLock lock = redissonClient.getLock(finalLockKey);
            boolean lockSuccess = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (lockSuccess) {
                try {
                    return proceeding.proceed();
                } finally {
                    lock.unlock();
                }
            } else {
                throw new RuntimeException("不能频繁操作，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("不能频繁操作:{}", e.getLocalizedMessage(), e);
            throw new RuntimeException("不能频繁操作，请稍后重试");
        }
    }

}
