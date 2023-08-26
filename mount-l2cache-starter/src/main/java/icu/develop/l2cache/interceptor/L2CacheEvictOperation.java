package icu.develop.l2cache.interceptor;

import lombok.Getter;
import lombok.Setter;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/13 17:21
 */
@Getter
public class L2CacheEvictOperation extends L2CacheOperation {

    private final boolean cacheWide;

    private final boolean beforeInvocation;

    /**
     * Create a new {@link L2CacheOperation} instance from the given builder.
     *
     * @param b 构造器
     * @since 4.3
     */
    protected L2CacheEvictOperation(Builder b) {
        super(b);
        this.cacheWide = b.cacheWide;
        this.beforeInvocation = b.beforeInvocation;
    }

    @Setter
    public static class Builder extends L2CacheOperation.Builder {

        private boolean cacheWide = false;
        private boolean beforeInvocation = false;

        @Override
        protected StringBuilder getOperationDescription() {
            StringBuilder sb = super.getOperationDescription();
            sb.append(",");
            sb.append(this.cacheWide);
            sb.append(",");
            sb.append(this.beforeInvocation);
            return sb;
        }

        @Override
        public L2CacheEvictOperation build() {
            return new L2CacheEvictOperation(this);
        }
    }
}
