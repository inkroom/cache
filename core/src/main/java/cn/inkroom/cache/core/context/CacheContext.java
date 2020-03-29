package cn.inkroom.cache.core.context;

import cn.inkroom.cache.core.annotation.Cache;
import cn.inkroom.cache.core.annotation.Caches;
import cn.inkroom.cache.core.config.CacheProperties;
import cn.inkroom.cache.core.script.ScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 缓存相关上下文数据
 *
 * @author 墨盒
 * @date 2020/3/7
 */
public class CacheContext {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String id;
    private Cache cache;
    private Caches caches;

    private String key;
    private CacheProperties properties;

    private ScriptEngine engine;
    private Map<String, Object> args;

    private Object returnValue;

    /**
     * @param id         方法路径，如: com.apache.Core.new(int,String)
     * @param cache      缓存注解
     * @param properties 相关配置项
     * @param engine     脚本执行引擎
     * @param args       方法参数
     */
    public CacheContext(String id, Caches caches, Cache cache, CacheProperties properties, ScriptEngine engine, Map<String, Object> args) {
        this.id = id;
        this.caches = caches;
        this.cache = cache;
        this.properties = properties;
        this.engine = engine;
        this.args = args;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;

        args.put("rv", returnValue);

    }

    /**
     * @return
     */
    public String getKey() {
        if (key != null) return key;
        return key = getKey(this.cache);
    }

    /**
     * @return
     */
    public String getKey(Cache cache) {

        return (properties.getKeyPrefix() + engine.express(cache.key(), args));
    }

    /**
     * @return
     */
    public String getKey(String key) {
        return (properties.getKeyPrefix() + engine.express(key, args));
    }

    /**
     * 是否忽略缓存
     *
     * @return
     */
    public boolean isIgnore() {
        boolean ignore = false;
        if (!cache.ignore().equals("")) {
            ignore = engine.booleanExpress(cache.ignore(), args);
        }

        return ignore;
    }

    public long expire() {
        return cache.expire();
    }

    public long getTtl(Cache cache) {
        long ttl = cache.ttl();
        if (cache.random().length == 0)
            return ttl;
        int[] randoms = cache.random();
        Random random = new Random();
        return ttl + (random.nextInt(randoms[1] - randoms[0]) + randoms[0]) * 1000;
    }

    public long getTtl() {

        return getTtl(this.cache);
    }

    /**
     * 判断是否要存入缓存
     * <p>通过 #params 访问参数</p>
     * <p>通过 #rv 访问结果</p>
     *
     * @return
     * @see Cache#condition()
     */
    public boolean isSave() {
        return isSave(this.cache);
    }

    /**
     * 判断是否要存入缓存
     * <p>通过 #params 访问参数</p>
     * <p>通过 #rv 访问结果</p>
     *
     * @return
     * @see Cache#condition()
     */
    public boolean isSave(Cache cache) {
        if (returnValue == null && !cache.nullable()) return false;
        return isSave(cache.condition());
    }

    public boolean isSave(String condition) {
        if ("".equals(cache.condition())) return true;
        logger.debug("condition脚本={}", cache.condition());
        logger.debug("condition的context={}", args);
        return engine.booleanExpress(cache.condition(), args);
    }

    /**
     * 获取指定的属性数据
     *
     * @param cache 缓存
     * @return
     */
    public Object getData(Cache cache) {
        return engine.objectExpress(cache.data(), this.args);
    }

    public Caches getCaches() {
        return caches;
    }
}
