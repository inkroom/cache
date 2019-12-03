package cn.inkroom.cache.core.plugins;

import cn.inkroom.cache.core.annotation.Cache;

import java.util.Map;

/**
 * 插件，主要用于缓存使用情况统计
 */
public interface StaticsPlugin {
    /**
     * 缓存穿透
     *
     * @param id    唯一id，方法全路径
     * @param cache 缓存配置信息
     * @param key   实际访问的key
     * @param args  本次访问传递的上下文参数
     * @param first 是否是在第一次获取缓存时miss，未启用同步的情况下，该值永远为true
     */
    void miss(String id, Cache cache, String key, Map<String, Object> args, boolean first);

    /**
     * 首次缓存命中
     *
     * @param id    唯一id，方法全路径
     * @param cache 缓存配置信息
     * @param key   实际访问的key
     * @param args  本次访问传递的上下文参数
     * @param first 是否是在第一次获取缓存时miss，未启用同步的情况下，该值永远为true
     */
    void hit(String id, Cache cache, String key, Map<String, Object> args, boolean first);

}
