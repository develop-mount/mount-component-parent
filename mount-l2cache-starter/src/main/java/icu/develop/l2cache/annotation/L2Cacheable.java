package icu.develop.l2cache.annotation;

import icu.develop.l2cache.interceptor.L2CacheStrategy;
import icu.develop.l2cache.interceptor.NotCacheNullCacheStrategy;
import icu.develop.l2cache.constant.L2CacheConstant;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 查询时设缓存注解
 *
 * @author ：LiWei
 * @version ：B1.19.9
 * description ：redis 缓存注解
 * program ：bmp-prm
 * @date 2022年8月30日 09点46分
 */

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface L2Cacheable {

    @AliasFor("cacheNames")
    String[] value() default {L2CacheConstant.DEFAULT_CACHE_NAME};

    @AliasFor("value")
    String[] cacheNames() default {L2CacheConstant.DEFAULT_CACHE_NAME};

    /**
     * FIX: 固定设置; REQUEST_ARGS: 根据请求参数拼值;EXPRESSION: EL表达式
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
