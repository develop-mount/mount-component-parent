package icu.develop.l2cache.interceptor;

import icu.develop.l2cache.L2Cache;
import icu.develop.l2cache.L2CacheManager;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/13 21:35
 */
public class DefaultL2CacheResolver implements L2CacheResolver {

    private Set<String> getCacheNames(L2CacheOperationInvocationContext<?> context) {
        return context.getOperation().getCacheNames();
    }

    @Override
    public Collection<? extends L2Cache> resolveCaches(L2CacheOperationInvocationContext<?> context) {

        Set<String> cacheNames = getCacheNames(context);
        if (StringUtils.isEmpty(cacheNames)) {
            return Collections.emptyList();
        }
        Collection<L2Cache> result = new ArrayList<>(1);
        for (String cacheName : cacheNames) {
            L2Cache cache = L2CacheManager.getCache(cacheName);
            if (cache == null) {
                throw new IllegalArgumentException("Cannot find cache named '" +
                        cacheName + "' for " + context.getOperation());
            }
            result.add(cache);
        }
        return result;
    }

}
