package icu.develop.lock.exception;

/**
 * @author linfeng
 * @description: TODO
 * @date 2023/12/6 9:18
 */
public class RedisLockException extends Exception {
    public RedisLockException(String message) {
        super(message);
    }
}
