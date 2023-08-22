package icu.develop.l2cache.listener;

import icu.develop.l2cache.L2CacheManager;
import icu.develop.l2cache.constant.L2CacheConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/7 10:01
 */
@Slf4j
public class L2CacheKeyExpirationListener extends KeyExpirationEventMessageListener {

    /**
     * Creates new {@link L2CacheKeyExpirationListener} for {@code __keyevent@*__:expired} messages.
     *
     * @param listenerContainer must not be {@literal null}.
     */
    public L2CacheKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        String[] split = expiredKey.split(L2CacheConstant.L2CACHE_KEY_SPLIT, 2);
        if (split.length >= 1 && L2CacheConstant.L2CACHE_PREFIX.equals(split[0])) {
            L2CacheManager.clearCache(expiredKey);
        }
    }
}
