package icu.develop.l2cache.interceptor;

import lombok.Getter;
import lombok.Setter;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/13 17:25
 */
@Getter
public class L2CachePutOperation extends L2CacheOperation {

    /**
     * 允许缓存
     */
    private final Class<? extends L2CacheStrategy> strategy;

    /**
     * Create a new {@link L2CacheOperation} instance from the given builder.
     *
     * @param b 构造器
     * @since 4.3
     */
    protected L2CachePutOperation(Builder b) {
        super(b);
        this.strategy = b.strategy;
    }

    @Setter
    public static class Builder extends L2CacheOperation.Builder {

        /**
         * 允许缓存
         */
        private Class<? extends L2CacheStrategy> strategy;

        public void setStrategy(Class<? extends L2CacheStrategy> strategy) {
            this.strategy = strategy;
        }

        @Override
        protected StringBuilder getOperationDescription() {
            StringBuilder sb = super.getOperationDescription();
            sb.append(" | strategy='");
            sb.append(this.strategy);
            sb.append("'");
            return sb;
        }

        @Override
        public L2CachePutOperation build() {
            return new L2CachePutOperation(this);
        }
    }
}
