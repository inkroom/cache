package cn.inkroom.cache.core;

import cn.inkroom.cache.core.annotation.Cache;
import cn.inkroom.cache.core.config.CacheProperties;
import cn.inkroom.cache.core.db.CacheTemplate;
import cn.inkroom.cache.core.plugins.StaticsPlugin;
import cn.inkroom.cache.core.script.AviatorEngine;
import cn.inkroom.cache.core.script.ScriptEngine;
import cn.inkroom.cache.core.sync.SyncTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.support.ReflectionHelper;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 缓存核心类
 *
 * @author 墨盒
 * @date 2019/10/27
 */
public class CacheCore {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private SyncTool syncTool;

    private Map<String, Cache> methodCacheMap = new HashMap<>();

    private ExecutorService executor;
    private ScriptEngine engine = new AviatorEngine();

    private CacheProperties properties;

    public void setProperties(CacheProperties properties) {
        this.properties = properties;
    }

    private CacheTemplate cacheTemplate;
    private StaticsPlugin plugin;

    public CacheCore() {

        executor = Executors.newFixedThreadPool(20);
    }


    public CacheCore(CacheTemplate cacheTemplate) {
        super();
        this.cacheTemplate = cacheTemplate;
    }

    /**
     * 从缓存中query
     *
     * @param args    参数
     * @param id      对应方法的全路径，包含参数签名
     * @param task    获取实际数据的回调
     * @param wrapper 对返回值的额外处理，用于绕开部分框架可能存在的对数据的包装
     * @return
     */
    @Deprecated
    public Object query(String id, Map<String, Object> args, Task task, ReturnValueWrapper wrapper) throws Throwable {

        Cache cache = getCache(id);
        return query(cache, id, args, task, wrapper);
    }


    public Object query(Cache cache, String id, Map<String, Object> args, Task task, ReturnValueWrapper wrapper) throws Throwable {
        if (cache == null) {
            return task.proceed();
        }
        methodCacheMap.put(id, cache);

        //获取key
        String key = properties.getKeyPrefix() + engine.express(cache.key(), args);
        //从redis中获取值
        Object value = getValue(key, cache, task);

        if (value != null) {
            plugin(id, cache, key, args, 1);
            return value;
        }
        //第一次穿透
        plugin(id, cache, key, args, 2);
        //是否启用锁
        if (lock(cache, key)) {
            //二次获取值
            //一般情况下，如果第二次获取成功，那么此时缓存多半是刚更新，此时忽略主动更新功能
            value = cacheTemplate.get(key);
            if (value != null) {
                //第二次命中
                plugin(id, cache, key, args, 3);
                unlock(cache, key);
                return value;
            }
        }
        plugin(id, cache, key, args, 4);
        value = task.proceed();
        //存入redis
        if (isSave(cache, args, value)) {
            cacheTemplate.set(key, wrapper == null ? value : wrapper.wrapper(value), getTtl(cache));
        }
        unlock(cache, key);
        return value;
    }

    /**
     * @param id
     * @param cache
     * @param key
     * @param args
     * @param flag  1第一次命中，2第一次穿透，3第二次命中，4第二次穿透
     */
    private void plugin(String id, Cache cache, String key, Map<String, Object> args, int flag) {
        if (plugin != null) {
            switch (flag) {
                case 1:
                    plugin.hit(id, cache, key, args, true);
                    break;
                case 2:
                    plugin.miss(id, cache, key, args, true);
                    break;
                case 3:
                    plugin.hit(id, cache, key, args, false);
                    break;
                case 4:
                    plugin.miss(id, cache, key, args, false);
                    break;
            }
        }

    }

    /**
     * 删除缓存
     * 用于update时
     *
     * @param id
     * @param args
     * @return
     */
    public boolean del(String id, Map<String, Object> args) throws Throwable {
        Cache cache = getCache(id);
        return del(cache, id, args);
    }

    public boolean del(Cache cache, String id, Map<String, Object> args) throws Throwable {
        if (cache == null || !cache.del()) return false;
        //获取key
        String key = engine.express(cache.key(), args);
        return cacheTemplate.del(key);
    }

    /**
     * 判断是否要存入缓存
     * <p>通过 #params 访问参数</p>
     * <p>通过 #rv 访问结果</p>
     *
     * @param cache       配置
     * @param params      参数
     * @param returnValue 可能要缓存的结果
     * @return
     * @see Cache#condition()
     */
    private boolean isSave(Cache cache, Map<String, Object> params, Object returnValue) {
        if (returnValue == null && !cache.nullable()) return false;
        if ("".equals(cache.condition())) return true;
        logger.debug("condition脚本={}", cache.condition());
        Map<String, Object> args = new HashMap<>();
        args.put("params", params);
        args.put("rv", returnValue);

        logger.debug("condition的context={}", args);
        return engine.booleanExpress(cache.condition(), args);
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
                executor.execute(() -> {
                    try {
                        Object proceed = task.proceed();
                        cacheTemplate.set(key, proceed, getTtl(cache));
                        logger.debug("主动更新值={}", key);
                    } catch (Throwable throwable) {
                        logger.warn("[获取数据] - 主动更新缓存时获取数据失败 {}", throwable.getMessage(), throwable);
                    }
                });

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
        if (cache.sync() || properties.isSync()) {
            syncTool.lock(key);
            return true;
        }
        return false;
    }

    private void unlock(Cache cache, String key) {
        if (cache.sync() || properties.isSync()) {
            syncTool.unlock(key);
        }
    }

    private Pattern idPattern = Pattern.compile("^(^[\\(]+)([^.|\\(]+)\\((.+)\\)");


    /**
     * 获取Cache注解和返回值类型
     *
     * @param id 方法全路径，如cn.inkroom.cache.core.CacheCore.getCache(String)
     * @return cache
     * @throws Throwable
     */
    @SuppressWarnings("all")
    private Cache getCache(String id) throws Throwable {

        logger.debug("缓存的cache={}", methodCacheMap);
        //从缓存中拿
        if (methodCacheMap.containsKey(id)) {
            return methodCacheMap.get(id);
        }
        //id 是方法全路径，不包括参数
//TODO 2019/12/20 此处有问题
        Matcher matcher = idPattern.matcher(id);
        Method method = null;
        if (matcher.find()) {
            Class c = Class.forName(matcher.group(1));

            Method[] methods = c.getMethods();

            List<Class> params = new LinkedList<>();

            for (int i = 3; i <= matcher.groupCount(); i++) {
                params.add(Class.forName(matcher.group(i)));
            }
            Class[] ps = new Class[params.size()];
            if (params.size() != 0) {
                method = c.getMethod(matcher.group(2), params.toArray(ps));
            } else {
                method = c.getMethod(matcher.group(2));
            }

        }

        Cache c = method.getAnnotation(Cache.class);
        methodCacheMap.put(id, c);
        return c;
    }


    public void setEngine(ScriptEngine engine) {
        this.engine = engine;
    }

    public SyncTool getSyncTool() {
        return syncTool;
    }

    public void setSyncTool(SyncTool syncTool) {
        this.syncTool = syncTool;
    }

    public CacheTemplate getCacheTemplate() {
        return cacheTemplate;
    }

    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    public void setPlugin(StaticsPlugin plugin) {
        this.plugin = plugin;
    }

}
