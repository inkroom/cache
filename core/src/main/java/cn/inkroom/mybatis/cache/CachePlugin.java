package cn.inkroom.mybatis.cache;

import cn.inkroom.mybatis.cache.annotation.Cache;
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
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 缓存插件
 *
 * @author 墨盒
 * @date 2019/10/26
 */
//@Intercepts({
//        @Signature(
//                type = Executor.class,
//                method = "update",
//                args = {MappedStatement.class, Object.class}
//        ),
//        @Signature(
//                type = Executor.class,
//                method = "query",
//                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
//        ),
//        @Signature(
//                type = StatementHandler.class,
//                method="query",
//                args = {Statement.class,ResultHandler.class}
//        )
//}
//)
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
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class CachePlugin implements Interceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private SyncLock lock;

    private Map<String, Cache> methodCacheMap = new HashMap<>();
    private Map<String, Object> ognlMap = new HashMap<>();


    public CachePlugin() {
    }

    /**
     * 是否启用同步；可在mybatis xml中配置，也可以在dao 方法中注解
     * <p>注解权限高于xml</p>
     */
    private boolean lockable = false;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        logger.debug("target=-{}", invocation.getTarget());
        logger.debug("method={}", invocation.getMethod());

        //封装参数
        Map<String, Object> args = getArgs(invocation);

        org.apache.ibatis.mapping.MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        logger.debug("id={}", mappedStatement.getId());
        getCache(mappedStatement);
//获取注解
        Cache cache = getCache(mappedStatement);
        String key = getKey(mappedStatement.getId(), cache, args);
        //解析key
//        Object expression = Ognl.parseExpression(cache.key());
//        String key = Ognl.getValue(expression, args, String.class);

        logger.debug("解析出来的key={}", key);
        return invocation.proceed();
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

//    private String getKey(Cache cache){
//
//    }

    private Cache getCache(MappedStatement ms) throws Exception {
        String id = ms.getId();
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
                return methodCacheMap.put(id, c);
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
