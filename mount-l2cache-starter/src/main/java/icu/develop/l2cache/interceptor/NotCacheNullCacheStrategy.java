package icu.develop.l2cache.interceptor;

import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Objects;

/**
 * Description:
 * 不缓存空值
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/7 15:34
 */
public class NotCacheNullCacheStrategy implements L2CacheStrategy {
    @Override
    public boolean enableCache(Object result) {
        // 不为空，则缓存
        return !empty(result);
    }

    private boolean empty(Object value) {
        if (Objects.isNull(value)) {
            return true;
        }
        if (value instanceof Collection) {
            //noinspection rawtypes
            Collection collect = (Collection) value;
            return CollectionUtils.isEmpty(collect);
        }
        return false;
    }
}
