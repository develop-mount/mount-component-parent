package icu.develop.l2cache.interceptor;

import lombok.Getter;
import lombok.Setter;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/13 15:04
 */
@Getter
public class L2CacheableOperation extends L2CacheOperation {

    /**
     * 缓存策略
     */
    private final Class<? extends L2CacheStrategy> strategy;

    public L2CacheableOperation(L2CacheableOperation.Builder b) {
        super(b);
        this.strategy = b.strategy;
    }

    @Setter
    public static class Builder extends L2CacheOperation.Builder {

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
        public L2CacheableOperation build() {
            return new L2CacheableOperation(this);
        }
    }
}
