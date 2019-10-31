package cn.inkroom.cache.mybatis;

import cn.inkroom.cache.core.CacheCore;
import cn.inkroom.cache.core.ReturnValueWrapper;
import cn.inkroom.cache.core.annotation.Cache;
import cn.inkroom.cache.core.sync.JdkSyncTool;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.Statement;
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
        ),
}
)
public class CachePlugin implements Interceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private CacheCore core;
    private Map<String, ReturnValueWrapper> returnValueTypeMap = new HashMap<>();

    public void setCore(CacheCore core) {
        this.core = core;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        logger.debug("target=-{}", invocation.getTarget());
        logger.debug("method={}", invocation.getMethod());

        /*
        由于mybatis自身流程(Select结果在Executor 都是List)，如果直接对Executor 的结果做缓存判断，那么会出现类型错误
        因此需要对ResultSetHandler 做拦截，获取真正的返回值类型和结果
         */
        if (invocation.getMethod().getName().equals("query")) {
            //查询
            return query(invocation);
        } else {//修改，直接删除指定key
            Object value = invocation.proceed();
            logger.debug("v ={} {}", value.getClass(), value);
            del(invocation);
            return value;
        }
    }

    /**
     * 执行查询任务
     *
     * @param invocation
     * @return
     */
    private Object del(final Invocation invocation) throws Throwable {
        //获取Cache
        org.apache.ibatis.mapping.MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        //封装参数
        Map<String, Object> args = getArgs(invocation);
        return core.del(mappedStatement.getId(), args);
    }


    /**
     * 执行查询任务
     *
     * @param invocation
     * @return
     */
    private Object query(final Invocation invocation) throws Throwable {
        //获取Cache
        org.apache.ibatis.mapping.MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        //封装参数
        Map<String, Object> args = getArgs(invocation);
        return core.query(mappedStatement.getId(), args, invocation::proceed, getReturnValueWrapper(mappedStatement));
    }

    private ReturnValueWrapper getReturnValueWrapper(MappedStatement mappedStatement) throws Throwable {
        if (returnValueTypeMap.containsKey(mappedStatement.getId())) {
            return returnValueTypeMap.get(mappedStatement.getId());
        }
        String id = mappedStatement.getId();
        //id 是方法全路径，不m包括参数
        String className = id.substring(0, id.lastIndexOf("."));

        String methodName = id.substring(id.lastIndexOf(".") + 1);

        Method[] methods = Class.forName(className).getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                ReturnValueWrapper wrapper = new MybatisReturnValueWrapper(method.getReturnType());
                returnValueTypeMap.put(id, wrapper);
                return wrapper;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getArgs(Invocation invocation) {
        return ((Map<String, Object>) invocation.getArgs()[1]);
    }

    @Override
    public void setProperties(Properties properties) {
        if (properties.get("syncClass") == null) {
            this.core.setSyncTool(new JdkSyncTool());
        }
        this.core.setSync(Boolean.parseBoolean(properties.getOrDefault("syncable", false).toString()));
    }
}
