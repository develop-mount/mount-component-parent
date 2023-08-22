package icu.develop.l2cache.interceptor;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/7 15:26
 */
public interface L2CacheStrategy {
    /**
     * 允许缓存
     *
     * @param result 结果
     * @return 是否缓存
     */
    boolean enableCache(Object result);
}
