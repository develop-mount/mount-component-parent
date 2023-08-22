package icu.develop.l2cache;

import java.lang.reflect.Type;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/4 18:00
 */
public interface L2Cache {

    /**
     * @return
     */
    String name();

    /**
     * 克隆二级缓存
     *
     * @param name 缓存名称
     * @return 二级缓存
     */
    L2Cache newCache(String name);

    /**
     * 查询值
     *
     * @param key  key
     * @param type type
     * @return 值
     */
    <T> T get(String key, Type type);

    /**
     * 存储到缓存
     *
     * @param key   key
     * @param value 值
     */
    <T> void put(String key, T value);

    /**
     * 删除缓存
     *
     * @param key key
     */
    void delete(String key);

    /**
     * 删除全部缓存
     */
    void clear();
}
