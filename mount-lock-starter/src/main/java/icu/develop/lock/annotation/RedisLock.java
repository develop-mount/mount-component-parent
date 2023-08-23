package icu.develop.lock.annotation;

import java.lang.annotation.*;

/**
 * Redis分布式锁
 *
 * @author ：linfeng
 * program ：bmp-prm
 * @date 2022年8月30日 09点46分
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLock {

    /**
     * 分布式锁定Key
     *
     * @return 锁定Key
     */
    String lockKey() default "";

    /**
     * 等待获取锁定的实际，单位秒
     */
    long waitTime() default 2;

    /**
     * 锁定时间，单位：秒 ；默认：长期
     */
    long lockedTime() default -1;
}
