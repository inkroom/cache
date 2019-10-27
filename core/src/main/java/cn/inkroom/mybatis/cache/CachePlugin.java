package cn.inkroom.mybatis.cache;

import cn.inkroom.mybatis.cache.annotation.Cache;
import cn.inkroom.mybatis.cache.db.CacheTemplate;
import cn.inkroom.mybatis.cache.ognl.CacheOgnlContext;
import cn.inkroom.mybatis.cache.sync.JdkSyncLock;
import cn.inkroom.mybatis.cache.sync.SyncLock;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.ognl.Ognl;
import org.apache.ibatis.ognl.OgnlContext;
import org.apache.ibatis.ognl.OgnlException;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 缓存插件
 *
 * @author 墨盒
 * @date 2019/10/26
 */
@Intercepts(value = {
        @Signature(type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                        CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}
        )
}
)
public class CachePlugin implements Interceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private SyncLock lock;

    private Map<String, Cache> methodCacheMap = new HashMap<>();
    private Map<String, Object> ognlMap = new HashMap<>();


    private CacheTemplate cacheTemplate;
    /**
     * 是否启用同步；可在mybatis xml中配置，也可以在dao 方法中注解
     * <p>注解权限高于xml</p>
     */
    private boolean lockable = false;

    @Resource(name = "cacheTemplate")
    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        logger.debug("注入 redis template");
        this.cacheTemplate = cacheTemplate;
    }

    @Resource(name = "syncLock")
    public void setLock(SyncLock lock) {
        this.lock = lock;
    }

    public CachePlugin(boolean lockable) {
        this.lockable = lockable;
    }

    public void setLockable(boolean lockable) {
        this.lockable = lockable;
    }

    public CachePlugin(SyncLock lock) {
        this.lock = lock;
    }

    public CachePlugin(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    public CachePlugin() {
        logger.debug("插件被创建");
    }


    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        logger.debug("target=-{}", invocation.getTarget());
        logger.debug("method={}", invocation.getMethod());


        if (invocation.getMethod().getName().equals("query")) {
            //查询
            return query(invocation);
        } else {//修改
            return invocation.proceed();
        }
    }

    /**
     * 执行查询任务
     *
     * @param invocation
     * @return
     */
    private Object query(Invocation invocation) throws Throwable {
        //获取Cache
        org.apache.ibatis.mapping.MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Cache cache = getCache(mappedStatement);

        if (cache == null) {//没有注解，直接执行sql
            return invocation.proceed();
        }
        //封装参数
        Map<String, Object> args = getArgs(invocation);
        //获取缓存key
        String key = getKey(mappedStatement.getId(), cache, args);

        //从redis中获取
        Object redisValue = cacheTemplate.get(key);

        //拿到redis值
        if (redisValue != null) {
            logger.debug("第一次从redis获取值 ，key={}", key);
            return redisValue;
        }

        //是否启用lock
        if (cache.sync() || this.lockable) {
            logger.debug("启用锁,key-{}", key);
            //启用同步
            lock.lock(key);

            //再次从redis中获取值
            redisValue = cacheTemplate.get(key);
            //拿到redis值
            if (redisValue != null) {
                lock.unlock(key);
                logger.debug("二次从redis中获取值成功，key={}", key);
                return redisValue;
            }
            //此时方才执行sql
            redisValue = invocation.proceed();
        } else {
            redisValue = invocation.proceed();
        }

        //存入redis
        if (cache.nullable() && redisValue == null) {
            cacheTemplate.set(key, null, cache.ttl());
        } else if (redisValue != null) {
            cacheTemplate.set(key, redisValue, cache.ttl());
        }
        //执行完毕才解锁，以便于后续线程进入
        if (cache.sync() || this.lockable) {
            lock.unlock(key);
            logger.debug("执行完sql，解锁key={}", key);
        }


        return redisValue;


    }


    /**
     * 获取缓存用的key
     *
     * @param id    dao方法全路径
     * @param cache 缓存注解
     * @param args  dao的参数
     * @return
     */
    private String getKey(String id, Cache cache, Map<String, Object> args) {
        Object expression = ognlMap.get(id);

        try {
            if (expression == null) {
                expression = Ognl.parseExpression(cache.key());
                ognlMap.put(id, expression);
            }

            //必须提供一个MemberAccess但是没有用
            OgnlContext context = CacheOgnlContext.getContext();

            context.put("context", "文本");


            return ((String) Ognl.getValue(expression, context, args, String.class));
        } catch (OgnlException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getArgs(Invocation invocation) {
        return ((Map<String, Object>) invocation.getArgs()[1]);
    }

    /**
     * 此处需要注意线程安全
     *
     * @param ms
     * @return
     * @throws Exception
     */
    private Cache getCache(MappedStatement ms) throws Exception {
        String id = ms.getId();
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


    @Override
    public Object plugin(Object target) {
        logger.debug("plugins ={}", target);
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        if (properties.get("syncClass") == null) {
            this.lock = new JdkSyncLock();
        }
        this.lockable = Boolean.parseBoolean(properties.getOrDefault("syncable", false).toString());
    }
}
