package cn.inkroom.cache.core;

import cn.inkroom.cache.core.annotation.Cache;
import cn.inkroom.cache.core.annotation.CacheEvict;
import cn.inkroom.cache.core.annotation.Caches;
import cn.inkroom.cache.core.config.CacheProperties;
import cn.inkroom.cache.core.context.CacheContext;
import cn.inkroom.cache.core.db.CacheTemplate;
import cn.inkroom.cache.core.plugins.StaticsPlugin;
import cn.inkroom.cache.core.script.AviatorEngine;
import cn.inkroom.cache.core.script.ScriptEngine;
import cn.inkroom.cache.core.sync.SyncTool;
import cn.inkroom.cache.core.util.ReflectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 缓存核心类
 *
 * @author 墨盒
 * @date 2019/10/27
 */
public class CacheCore {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private SyncTool syncTool;

    private ExecutorService executor;
    private ScriptEngine engine = new AviatorEngine();

    private CacheProperties properties;

    public void setProperties(CacheProperties properties) {
        this.properties = properties;
    }

    private CacheTemplate cacheTemplate;
    private StaticsPlugin plugin;

    private ReflectHelper helper = new ReflectHelper();

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

    public Object query(String id, Map<String, Object> args, Task task, ReturnValueUnWrapper wrapper) throws Throwable {

        Caches caches = helper.getCaches(id);

        Cache cache = helper.getCache(id);
        return query(caches, cache, id, args, task, wrapper);
    }

    private Object query(Caches caches, Cache cache, String id, Map<String, Object> args, Task task, ReturnValueUnWrapper wrapper) throws Throwable {
        if (caches == null && cache == null) {
            return task.proceed();
        }


//        构建上下文对象
        CacheContext context = new CacheContext(id, caches, cache, properties, engine, args);
        //获取key
        String key = context.getKey();
        //从redis中获取值
        Object value = getCacheValue(key, caches, context, task, wrapper);

        if (value != null) {//命中缓存
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
        value = task.proceed();//执行真实方法
        context.setReturnValue(value);
        //存入redis

        saveValue(context, wrapper);
//        if (isSave(caches, cache, args, value)) {
//            cacheTemplate.set(key, wrapper == null ? value : wrapper.wrapper(value), getTtl(cache));
//        }
        unlock(cache, key);

//        实现删除缓存功能


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
        Cache cache = helper.getCache(id);
        return del(cache, id, args);
    }

    private boolean del(Cache cache, String id, Map<String, Object> args) throws Throwable {
        if (cache == null || !cache.del()) return false;
        //获取key
        String key = engine.express(cache.key(), args);
        return cacheTemplate.del(key);
    }

    /**
     * 获取value，并在满足情况下，主动更新缓存
     *
     * @param key   key
     * @param cache 配置信息
     * @param task  真正获取数据的方法
     * @return
     */
    private Object getCacheValue(String key, Caches caches, CacheContext context, Task task, ReturnValueUnWrapper wrapper) throws Throwable {
        if (context.isIgnore()) {//是否需要忽略缓存
            return null;
        }

        Object value = cacheTemplate.get(key);
        if (value != null) {
            //最顶层对象的时间
            long expire = context.expire();
            if (expire == -1) {//不主动更新
                return value;
            } else if (expire < cacheTemplate.ttl(key)) {
                //另启线程更新数据
                executor.execute(() -> {
                    try {
                        Object proceed = task.proceed();
                        context.setReturnValue(proceed);
                        saveValue(context, wrapper);
                        logger.debug("主动更新值={}", key);
                    } catch (Throwable throwable) {
                        logger.warn("[获取数据] - 主动更新缓存时获取数据失败 {}", throwable.getMessage(), throwable);
                    }
                });

            }
        }
        return value;
    }

    /**
     * 存入缓存
     *
     * @param context 上下文信息
     * @param wrapper 数据包装解包装
     */
    private void saveValue(CacheContext context, ReturnValueUnWrapper wrapper) throws Throwable {

        Object v = wrapper == null ? context.getReturnValue() : wrapper.wrapper(context.getReturnValue());
        if (context.isSave()) {
            String key = context.getKey();
            Object nv = v;
//                if (i != 0 && !"".equals(cs[i].data())) {
//                    nv = engine.objectExpress(cs[i].data(), params);
//                }
            cacheTemplate.set(key, nv, context.getTtl());
        }
        //            保存属性数据

        if (context.getCaches() != null) {
            Cache[] cs = context.getCaches().value();

            for (Cache c : cs) {

                if (context.isSave(c)) {
                    cacheTemplate.set(context.getKey(c), context.getData(c), context.getTtl(c));
                }


            }
//            处理要删除的数据
            CacheEvict[] evicts = context.getCaches().evict();
            for (CacheEvict evict : evicts) {

                if (context.isSave(evict.condition())) {
                    String key = context.getKey(evict.key());

                    cacheTemplate.del(key);

                }
            }
        }
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
