package cn.inkroom.mybatis.cache.annotation;

import cn.inkroom.mybatis.cache.sync.JdkSyncLock;
import cn.inkroom.mybatis.cache.sync.SyncLock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 墨盒
 * @date 2019/10/26
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cache {
    /**
     * ognl表达式
     *
     * @return
     */
    String key();

    /**
     * ttl，单位毫秒
     *
     * @return
     */
    long ttl() default 5 * 60 * 1000;

    /**
     * 是否缓存空值
     *
     * @return
     */
    boolean nullable() default false;

    /**
     * 是否启用锁
     * <p>如果不上锁的情况下，在高并发时不能保证重复请求会从缓存中获取</p>
     * <p>锁使用双重判断模式，尽可能保证减少db请求，并减少锁带来的开销</p>
     *
     * @return
     */
    boolean sync() default false;

    /**
     * 锁的实现类
     *
     * @return
     */
    Class<? extends SyncLock> syncClass() default JdkSyncLock.class;


}
