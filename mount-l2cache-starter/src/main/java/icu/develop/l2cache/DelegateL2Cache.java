package icu.develop.l2cache;

import java.lang.reflect.Type;

import static icu.develop.l2cache.constant.L2CacheConstant.L2CACHE_PREFIX;

/**
 * Description:
 * 代理缓存
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/7 10:03
 */
public class DelegateL2Cache implements L2Cache {

    private final L2Cache delegate;

    public DelegateL2Cache(L2Cache delegate) {
        this.delegate = delegate;
    }

    @Override
    public String name() {
        return delegate.name();
    }

    @Override
    public L2Cache newCache(String name) {
        return new DelegateL2Cache(delegate.newCache(name));
    }

    @Override
    public <T> T get(String key, Type type) {
        return delegate.get(cacheKey(key), type);
    }

    @Override
    public <T> void put(String key, T value) {
        delegate.put(cacheKey(key), value);
    }

    @Override
    public void delete(String key) {
        delegate.delete(cacheKey(key));
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    private String cacheKey(String key) {
        return L2CACHE_PREFIX + key;
    }
}
