package icu.develop.l2cache;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static icu.develop.l2cache.constant.L2CacheConstant.*;

/**
 * Description:
 * 二级缓存管理
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/7 13:51
 */
public class L2CacheManager {

    /**
     * 缓存Map
     */
    private static final ConcurrentHashMap<String, L2Cache> CACHE_MAP = new ConcurrentHashMap<>();

    /**
     * 清理缓存
     *
     * @param key 缓存key
     */
    public static void clearCache(String key) {
        CACHE_MAP.values().forEach(cache -> {
            cache.delete(key);
        });
    }

    /**
     * 获取缓存
     *
     * @return 二级缓存
     */
    public static L2Cache getCache() {
        return CACHE_MAP.get(DEFAULT_CACHE_NAME);
    }

    /**
     * 获取缓存
     *
     * @param cacheName 缓存名称
     * @return 二级缓存
     */
    public static L2Cache getCache(String cacheName) {
        L2Cache cache = CACHE_MAP.get(cacheName);
        if (Objects.isNull(cache)) {
            L2Cache l2Cache = getCache().newCache(cacheName);
            putCache(cacheName, l2Cache);
            return l2Cache;
        }
        return cache;
    }

    /**
     * 注册二级缓存
     *
     * @param cacheName 缓存名称
     * @param cache     二级缓存
     */
    public static void putCache(String cacheName, L2Cache cache) {
        CACHE_MAP.putIfAbsent(cacheName, cache);
    }
}
