package icu.develop.limiter.annotation;

import java.lang.annotation.*;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/4/26 10:13
 */
@Target({ElementType.METHOD}) // 此注解只能用在方法上
@Retention(RetentionPolicy.RUNTIME) // 注解的作用域为JVM运行时
@Documented // 生成javadoc时包含该注解
@Inherited // 此注解允许被继承
public @interface RedisRateLimiter {
    /**
     * 限流标识key，每个http接口都应该有一个唯一的key。
     *
     * @return 限流器名称
     */
    String limiterKey();

    /**
     * 限流的时间(单位为:秒)，比如1分钟内最多1000个请求。注意我们这个限流器不是很精确，但误差不会太大
     *
     * @return 限流时间
     */
    long timeout();

    /**
     * 限流的次数，比如1分钟内最多1000个请求。注意count的值不能小于1,必须大于等于1
     *
     * @return 限流次数
     */
    long limitCount();
}
