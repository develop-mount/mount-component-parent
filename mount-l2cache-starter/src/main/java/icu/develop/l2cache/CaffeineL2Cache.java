package icu.develop.l2cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import icu.develop.l2cache.constant.L2CacheConstant;
import icu.develop.l2cache.utils.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 * Caffeine 缓存
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/4 18:12
 */
@Slf4j
public class CaffeineL2Cache implements L2Cache {

    private final com.github.benmanes.caffeine.cache.Cache<String, Object> caffeineCache;
    private final L2Cache delegate;
    private final long duration;

    public CaffeineL2Cache(L2Cache delegate, long duration) {
        this.delegate = delegate;
        this.duration = duration;
        this.caffeineCache = CaffeineL2CacheBuilder.cache(duration);
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public L2Cache newCache(String name) {
        return new CaffeineL2Cache(delegate.newCache(name), duration);
    }

    @Override
    public <T> T get(String key, Type type) {
        Object obj = caffeineCache.getIfPresent(key);
        if (Objects.nonNull(obj)) {
            //noinspection unchecked
            return (T) deserialize(obj, type);
        }
        return delegate.get(key, type);
    }

    @Override
    public synchronized <T> void put(String key, T value) {
        caffeineCache.put(key, serialize(value));
        delegate.put(key, value);
    }

    @Override
    public synchronized void delete(String key) {
        caffeineCache.invalidate(key);
        delegate.delete(key);
    }

    @Override
    public synchronized void clear() {
        caffeineCache.invalidateAll();
        delegate.clear();
    }

    private static Object serialize(@Nullable Object source) {
        if (source == null) {
            return L2CacheConstant.EMPTY;
        } else {
            return source;
        }
    }

    /**
     * 反序列号
     *
     * @param source 字符串
     * @param type   类型
     * @param <T>    泛型
     * @return 对象
     */
    private static <T> T deserialize(@Nullable Object source, Type type) {
        if (L2CacheConstant.EMPTY.equals(source)) {
            Class<?> objectClass = TypeUtils.getMapping((Type) type);
            try {
                //noinspection unchecked
                return (T) objectClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            //noinspection unchecked
            return (T) source;
        }
    }

    public static class CaffeineL2CacheBuilder {
        /**
         * 构造缓存
         *
         * @param duration 有效时间
         * @param <T>      类型
         * @return 缓存
         */
        public static <T> Cache<String, T> cache(long duration) {
            return cache(L2CacheConstant.MIN_SIZE, L2CacheConstant.MAX_SIZE, duration);
        }

        /**
         * 构建所有来的要缓存的key getCache
         *
         * @param minSize       最小大小
         * @param maxSize       最大大小
         * @param expireSeconds 有效期 单位秒
         * @param <T>           泛型
         * @return 缓存
         */
        public static <T> Cache<String, T> cache(int minSize, long maxSize, long expireSeconds) {
            if (expireSeconds <= 0) {
                return Caffeine.newBuilder()
                        //初始大小
                        .initialCapacity(minSize)
                        //最大数量
                        .maximumSize(maxSize).build();
            }
            return Caffeine.newBuilder()
                    //初始大小
                    .initialCapacity(minSize)
                    //最大数量
                    .maximumSize(maxSize)
                    //过期时间
                    .expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build();
        }
    }
}
