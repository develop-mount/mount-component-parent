package icu.develop.l2cache.interceptor;

import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/12 21:12
 */
public interface L2CacheOperationSource {
    /**
     * 是否是候选类
     * @param targetClass 目标类
     * @return 是否候选
     */
    default boolean isCandidateClass(Class<?> targetClass) {
        return true;
    }

    /**
     * Return the collection of cache operations for this method,
     * or {@code null} if the method contains no <em>cacheable</em> annotations.
     *
     * @param method      the method to introspect
     * @param targetClass the target class (may be {@code null}, in which case
     *                    the declaring class of the method must be used)
     * @return all cache operations for this method, or {@code null} if none found
     */
    @Nullable
    Collection<L2CacheOperation> getCacheOperations(Method method, @Nullable Class<?> targetClass);
}
