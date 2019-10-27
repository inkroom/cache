package cn.inkroom.cache.core;

import cn.inkroom.cache.core.annotation.Cache;
import cn.inkroom.cache.core.db.CacheTemplate;
import cn.inkroom.cache.core.db.RedisCacheTemplate;
import cn.inkroom.cache.core.script.ScriptEngine;
import cn.inkroom.cache.core.script.SpElEngine;
import cn.inkroom.cache.core.sync.SyncLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 缓存核心类
 *
 * @author 墨盒
 * @date 2019/10/27
 */
public class CacheCore {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private SyncLock lock;

    private Map<String, Cache> methodCacheMap = new HashMap<>();


    private ScriptEngine engine = new SpElEngine();

    private CacheTemplate cacheTemplate;
    private boolean sync = false;

    public CacheCore() {

    }


    public CacheCore(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    /**
     * 从缓存中query
     *
     * @param args 参数
     * @param id   对应方法的全路径
     * @return
     */
    public Object query(String id, Map<String, Object> args, Task task) throws Throwable {

        Cache cache = getCache(id);
        if (cache == null) {
            return task.proceed();
        }
        //获取key
        String key = engine.express(cache.key(), args);
        //从redis中获取值
        Object value = getValue(key, cache, task);

        if (value != null) {
            logger.debug("第一次获取值成功,key={}", key);
            return value;
        }
        //是否启用锁
        if (lock(cache, key)) {
            //二次获取值
            //一般情况下，如果第二次获取成功，那么此时缓存多半是刚更新，此时忽略主动更新功能
            value = cacheTemplate.get(key);
            if (value != null) {
                logger.debug("二次获取值成功,key={}", key);
                unlock(cache, key);
                return value;
            }
        }
        logger.debug("执行真正获取数据方法={}", key);
        value = task.proceed();
        //存入redis
        cacheTemplate.set(key, value, getTtl(cache));

        unlock(cache, key);

        return value;

    }

    /**
     * 获取value，并在满足情况下，主动更新缓存
     *
     * @param key   key
     * @param cache 配置信息
     * @param task  真正获取数据的方法
     * @return
     */
    private Object getValue(String key, Cache cache, Task task) throws Throwable {

        Object value = cacheTemplate.get(key);
        if (value != null) {
            long expire = cache.expire();
            if (expire == -1) {//不主动更新
                return value;
            } else if (expire < cacheTemplate.ttl(key)) {
                //另启线程更新数据
                // TODO: 2019/10/27 线程池
                new Thread(() -> {
                    try {
                        Object proceed = task.proceed();
                        cacheTemplate.set(key, proceed, getTtl(cache));
                        logger.debug("主动更新值={}", key);
                    } catch (Throwable throwable) {
                        logger.warn("[获取数据] - 主动更新缓存时获取数据失败 {}", throwable.getMessage(), throwable);
                    }
                }).start();
            }
        }
        return value;
    }

    private long getTtl(Cache cache) {
        long ttl = cache.ttl();
        if (cache.random().length == 0)
            return ttl;
        int[] randoms = cache.random();
        Random random = new Random();
        return ttl + (random.nextInt(randoms[1] - randoms[0]) + randoms[0]) * 1000;
    }

    private boolean lock(Cache cache, String key) {
        if (cache.sync() || sync) {
            lock.lock(key);
            return true;
        }
        return false;
    }

    private void unlock(Cache cache, String key) {
        if (cache.sync() || sync) {
            lock.unlock(key);
        }
    }

    private Cache getCache(String id) throws Throwable {

        logger.debug("缓存的cache={}", methodCacheMap);
        //从缓存中拿
        if (methodCacheMap.containsKey(id)) {
            return methodCacheMap.get(id);
        }
        //id 是方法全路径，不m包括参数
        String className = id.substring(0, id.lastIndexOf("."));

        String methodName = id.substring(id.lastIndexOf(".") + 1);

        Method[] methods = Class.forName(className).getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Cache c = method.getAnnotation(Cache.class);
                methodCacheMap.put(id, c);
                return c;
            }
        }

        return null;
    }


    public void setEngine(ScriptEngine engine) {
        this.engine = engine;
    }

    public SyncLock getLock() {
        return lock;
    }

    public void setLock(SyncLock lock) {
        this.lock = lock;
    }

    public CacheTemplate getCacheTemplate() {
        return cacheTemplate;
    }

    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }
}
