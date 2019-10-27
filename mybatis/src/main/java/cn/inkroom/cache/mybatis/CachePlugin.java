package cn.inkroom.cache.mybatis;

import cn.inkroom.cache.core.CacheCore;
import cn.inkroom.cache.core.Task;
import cn.inkroom.cache.core.sync.JdkSyncLock;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private CacheCore core;

    public void setCore(CacheCore core) {
        this.core = core;
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
    private Object query(final Invocation invocation) throws Throwable {
        //获取Cache
        org.apache.ibatis.mapping.MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        //封装参数
        Map<String, Object> args = getArgs(invocation);
        return core.query(mappedStatement.getId(), args, invocation::proceed);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getArgs(Invocation invocation) {
        return ((Map<String, Object>) invocation.getArgs()[1]);
    }

    @Override
    public void setProperties(Properties properties) {
        if (properties.get("syncClass") == null) {
            this.core.setLock(new JdkSyncLock());
        }
        this.core.setSync(Boolean.parseBoolean(properties.getOrDefault("syncable", false).toString()));
    }
}
