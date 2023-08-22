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
    @AliasFor("cacheNames")
    String[] value() default {L2CacheConstant.DEFAULT_CACHE_NAME};

    @AliasFor("value")
    String[] cacheNames() default {L2CacheConstant.DEFAULT_CACHE_NAME};

    /**
     * 缓存key的类型：1.固定设值；2.参数组值
     */
    CacheKeyType cacheKeyType() default CacheKeyType.FIX;

    /**
     * 缓存的key，cacheType=固定设值；此值生效
     */
    String cacheKey() default "";

    /**
     * 允许缓存
     *
     * @return 缓存判断类型
     */
    Class<? extends L2CacheStrategy> strategy() default NotCacheNullCacheStrategy.class;
}
