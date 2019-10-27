package cn.inkroom.cache.core.db;

/**
 * 缓存操作类
 *
 * @author 墨盒
 * @date 2019/10/27
 */
public interface CacheTemplate {
    /**
     * 存入缓存
     *
     * @param key   key
     * @param value 值
     * @param ttl   存活时间，单位毫秒
     */
    void set(String key, Object value, long ttl) throws Throwable;

    /**
     * 获取值
     *
     * @param key
     * @return
     * @throws Throwable
     */
    Object get(String key) throws Throwable;

    /**
     * 获取缓存有效期
     *
     * @param key key
     * @return 单位毫秒,-1永不过期，-2该key不存在
     * @throws Throwable
     */
    long ttl(String key) throws Throwable;
}
