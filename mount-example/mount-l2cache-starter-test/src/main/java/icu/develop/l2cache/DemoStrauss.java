package icu.develop.l2cache;

import com.vevor.prm.common.cache.interceptor.L2CacheStrategy;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/8 17:58
 */
public class DemoStrauss implements L2CacheStrategy {
    @Override
    public boolean enableCache(Object result) {
        if (result instanceof Demo) {
            return true;
        }
        return false;
    }
}
