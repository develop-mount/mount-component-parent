package icu.develop.l2cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import icu.develop.l2cache.constant.L2CacheConstant;
import icu.develop.l2cache.exceptions.SerializationException;
import icu.develop.l2cache.utils.ListUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static icu.develop.l2cache.constant.L2CacheConstant.DEL_SIZE;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/4 18:12
 */
@Slf4j
public class RedisL2Cache implements L2Cache {

    private static final String CACHE_KEYS = "L2CACHE:REDIS:KEYS";
    private final String name;
    private final StringRedisTemplate stringRedisTemplate;
    private final long duration;

    public RedisL2Cache(String name, StringRedisTemplate stringRedisTemplate, long duration) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
        this.duration = duration;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public L2Cache newCache(String name) {
        return new RedisL2Cache(name, stringRedisTemplate, duration);
    }

    @Override
    public <T> T get(String key, Type type) {
        try {
            return deserialize(stringRedisTemplate.opsForValue().get(key), type);
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("redis cache get key:{}", key);
            }
        }
    }

    @Override
    public synchronized <T> void put(String key, T value) {
        try {
            stringRedisTemplate.opsForValue().set(key, serialize(value), duration);
            stringRedisTemplate.opsForSet().add(CACHE_KEYS, key);
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("redis cache put key:{}, value:{}", key, value);
            }
        }
    }

    @Override
    public synchronized void delete(String key) {
        stringRedisTemplate.delete(key);
        stringRedisTemplate.opsForSet().remove(CACHE_KEYS, key);
    }

    @Override
    public synchronized void clear() {
        Set<String> allKeys = stringRedisTemplate.opsForSet().members(CACHE_KEYS);
        if (CollectionUtils.isEmpty(allKeys)) {
            return;
        }
        List<List<String>> partition = ListUtils.partition(new ArrayList<>(allKeys), DEL_SIZE);
        for (List<String> keys : partition) {
            stringRedisTemplate.delete(keys);
        }
    }

    /**
     * 序列号
     *
     * @param source 对象
     * @return 字符串
     */
    public static String serialize(@Nullable Object source) {
        if (source == null) {
            return L2CacheConstant.EMPTY;
        } else {
            try {
                return JSON.toJSONString(source);
            } catch (JSONException e) {
                throw new SerializationException("Could not write JSON: " + e.getMessage(), e);
            }
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
    public static <T> T deserialize(@Nullable String source, Type type) {
        Assert.notNull(type, "Deserialization type must not be null! Please provide Object.class to make use of Fastjson default typing.");
        if (!StringUtils.hasText(source)) {
            return null;
        } else {
            try {
                String src = source.trim();
                return JSON.parseObject(src, type);
            } catch (JSONException e) {
                throw new SerializationException("Could not read JSON: " + e.getMessage(), e);
            }
        }
    }
}
