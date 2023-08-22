package icu.develop.l2cache.interceptor;

import icu.develop.l2cache.annotation.CacheKeyType;
import lombok.Getter;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/12 21:11
 */
@Getter
public class L2CacheOperation {

    private final String name;

    private final Set<String> cacheNames;

    private final String cacheKey;

    private final CacheKeyType cacheKeyType;

    private final String toString;

    /**
     * Create a new {@link L2CacheOperation} instance from the given builder.
     *
     * @since 4.3
     */
    protected L2CacheOperation(Builder b) {
        this.name = b.name;
        this.cacheKeyType = b.cacheKeyType;
        this.cacheNames = b.cacheNames;
        this.cacheKey = b.cacheKey;
        this.toString = b.getOperationDescription().toString();
    }

    public abstract static class Builder {

        private String name = "";
        private CacheKeyType cacheKeyType;

        Set<String> cacheNames = Collections.emptySet();

        private String cacheKey = "";

        public void setName(String name) {
            this.name = name;
        }

        public void setCacheKeyType(CacheKeyType cacheKeyType) {
            this.cacheKeyType = cacheKeyType;
        }

        public void setCacheName(String cacheName) {
            Assert.hasText(cacheName, "Cache name must not be empty");
            this.cacheNames = Collections.singleton(cacheName);
        }

        public void setCacheNames(String... cacheNames) {
            this.cacheNames = new LinkedHashSet<>(cacheNames.length);
            for (String cacheName : cacheNames) {
                Assert.hasText(cacheName, "Cache name must be non-empty if specified");
                this.cacheNames.add(cacheName);
            }
        }

        public void setCacheKey(String cacheKey) {
            this.cacheKey = cacheKey;
        }

        /**
         * Return an identifying description for this caching operation.
         * <p>Available to subclasses, for inclusion in their {@code toString()} result.
         */
        protected StringBuilder getOperationDescription() {
            StringBuilder result = new StringBuilder(getClass().getSimpleName());
            result.append("[").append(this.name);
            result.append("] caches=").append(this.cacheNames);
            result.append(" | cacheKey='").append(this.cacheKey);
            result.append("' | cacheKeyType='").append(this.cacheKeyType);
            return result;
        }

        public abstract L2CacheOperation build();
    }
}
