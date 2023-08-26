package icu.develop.l2cache.annotation;

import icu.develop.l2cache.interceptor.NotCacheNullCacheStrategy;
import icu.develop.l2cache.interceptor.L2CacheStrategy;
import icu.develop.l2cache.constant.L2CacheConstant;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/11 11:35
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface L2CachePut {
    /**
     * 缓存名称
     * @return 缓存名称
     */
    @AliasFor("cacheNames")
    String[] value() default {L2CacheConstant.DEFAULT_CACHE_NAME};

    /**
     * 缓存名称
     * @return 缓存名称
     */
    @AliasFor("value")
    String[] cacheNames() default {L2CacheConstant.DEFAULT_CACHE_NAME};

    /**
     * 缓存key的类型：1.固定设值；2.参数组值
     * @return 缓存类型 {@link  CacheKeyType}
     */
    CacheKeyType cacheKeyType() default CacheKeyType.FIX;

    /**
     * 缓存的key，cacheType=固定设值；此值生效
     * @return 缓存key
     */
    String cacheKey() default "";

    /**
     * 允许缓存
     *
     * @return 缓存判断类型 {@link  L2CacheStrategy}
     */
    Class<? extends L2CacheStrategy> strategy() default NotCacheNullCacheStrategy.class;
}
