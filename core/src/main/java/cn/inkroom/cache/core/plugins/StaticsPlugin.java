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
     */
    void miss(String id, Cache cache, String key, Map<String, Object> args);

    /**
     * 首次缓存命中
     *
     * @param id    唯一id，方法全路径
     * @param cache 缓存配置信息
     * @param key   实际访问的key
     * @param args  本次访问传递的上下文参数
     */
    void hit(String id, Cache cache, String key, Map<String, Object> args);

    /**
     * 第二次访问缓存命中，仅在启用了同步的情况下会被调用
     *
     * @param id    唯一id，方法全路径
     * @param cache 缓存配置信息
     * @param key   实际访问的key
     * @param args  本次访问传递的上下文参数
     */
    void hitAgain(String id, Cache cache, String key, Map<String, Object> args);
}
