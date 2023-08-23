package icu.develop.limiter;

/**
 * Description:
 * 可执行限流接口
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/3/30 18:06
 */
public interface LimiterProceeding<T> {

    /**
     * 执行加分布式锁的方法
     *
     * @return 对象
     * @throws Throwable 异常
     */
    T proceed() throws Throwable;
}
