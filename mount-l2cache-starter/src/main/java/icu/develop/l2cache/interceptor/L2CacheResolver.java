package icu.develop.l2cache.interceptor;

import icu.develop.l2cache.L2Cache;

import java.util.Collection;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/12 22:20
 */
public interface L2CacheResolver {

    Collection<? extends L2Cache> resolveCaches(L2CacheOperationInvocationContext<?> context);
}
