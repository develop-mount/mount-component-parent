package icu.develop.l2cache.annotation;

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
public @interface L2Caching {
    L2Cacheable[] cacheable() default {};

    L2CachePut[] put() default {};

    L2CacheEvict[] evict() default {};
}
