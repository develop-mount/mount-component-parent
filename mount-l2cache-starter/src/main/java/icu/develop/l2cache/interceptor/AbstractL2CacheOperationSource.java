package icu.develop.l2cache.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.MethodClassKey;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/12 21:14
 */
@Slf4j
public abstract class AbstractL2CacheOperationSource implements L2CacheOperationSource {
    /**
     * Canonical value held in cache to indicate no caching attribute was
     * found for this method and we don't need to look again.
     */
    private static final Collection<L2CacheOperation> NULL_CACHING_ATTRIBUTE = Collections.emptyList();

    /**
     * Cache of CacheOperations, keyed by method on a specific target class.
     * <p>As this base class is not marked Serializable, the cache will be recreated
     * after serialization - provided that the concrete subclass is Serializable.
     */
    private final Map<Object, Collection<L2CacheOperation>> attributeCache = new ConcurrentHashMap<>(1024);

    /**
     * Determine the caching attribute for this method invocation.
     * <p>Defaults to the class's caching attribute if no method attribute is found.
     *
     * @param method      the method for the current invocation (never {@code null})
     * @param targetClass the target class for this invocation (may be {@code null})
     * @return {@link L2CacheOperation} for this method, or {@code null} if the method
     * is not cacheable
     */
    @Override
    @Nullable
    public Collection<L2CacheOperation> getCacheOperations(Method method, @Nullable Class<?> targetClass) {
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }

        Object cacheKey = getCacheKey(method, targetClass);
        Collection<L2CacheOperation> cached = this.attributeCache.get(cacheKey);

        if (cached != null) {
            return (cached != NULL_CACHING_ATTRIBUTE ? cached : null);
        } else {
            Collection<L2CacheOperation> cacheOps = computeCacheOperations(method, targetClass);
            if (cacheOps != null) {
                if (log.isTraceEnabled()) {
                    log.trace("Adding cacheable method '" + method.getName() + "' with attribute: " + cacheOps);
                }
                this.attributeCache.put(cacheKey, cacheOps);
            } else {
                this.attributeCache.put(cacheKey, NULL_CACHING_ATTRIBUTE);
            }
            return cacheOps;
        }
    }


    /**
     * Determine a cache key for the given method and target class.
     * <p>Must not produce same key for overloaded methods.
     * Must produce same key for different instances of the same method.
     *
     * @param method      the method (never {@code null})
     * @param targetClass the target class (may be {@code null})
     * @return the cache key (never {@code null})
     */
    protected Object getCacheKey(Method method, @Nullable Class<?> targetClass) {
        return new MethodClassKey(method, targetClass);
    }


    @Nullable
    private Collection<L2CacheOperation> computeCacheOperations(Method method, @Nullable Class<?> targetClass) {
        // Don't allow no-public methods as required.
        if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
            return null;
        }

        // The method may be on an interface, but we need attributes from the target class.
        // If the target class is null, the method will be unchanged.
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

        // First try is the method in the target class.
        Collection<L2CacheOperation> opDef = findCacheOperations(specificMethod);
        if (opDef != null) {
            return opDef;
        }

        // Second try is the caching operation on the target class.
        opDef = findCacheOperations(specificMethod.getDeclaringClass());
        if (opDef != null && ClassUtils.isUserLevelMethod(method)) {
            return opDef;
        }

        if (specificMethod != method) {
            // Fallback is to look at the original method.
            opDef = findCacheOperations(method);
            if (opDef != null) {
                return opDef;
            }
            // Last fallback is the class of the original method.
            opDef = findCacheOperations(method.getDeclaringClass());
            if (opDef != null && ClassUtils.isUserLevelMethod(method)) {
                return opDef;
            }
        }

        return null;
    }

    protected boolean allowPublicMethodsOnly() {
        return false;
    }

    /**
     * Subclasses need to implement this to return the caching attribute for the
     * given class, if any.
     *
     * @param clazz the class to retrieve the attribute for
     * @return all caching attribute associated with this class, or {@code null} if none
     */
    @Nullable
    protected abstract Collection<L2CacheOperation> findCacheOperations(Class<?> clazz);

    /**
     * Subclasses need to implement this to return the caching attribute for the
     * given method, if any.
     *
     * @param method the method to retrieve the attribute for
     * @return all caching attribute associated with this method, or {@code null} if none
     */
    @Nullable
    protected abstract Collection<L2CacheOperation> findCacheOperations(Method method);
}
