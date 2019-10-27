package cn.inkroom.cache.core.annotation;

import cn.inkroom.cache.core.sync.JdkSyncLock;
import cn.inkroom.cache.core.sync.SyncLock;

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
     * 当有效期少于指定值时，主动更新缓存
     * <p>默认不启用</p>
     * @return
     */
    long expire() default -1;

    /**
     * 获取key的脚本，具体语法和解析引擎相关
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

    /**
     * 是否在有效期添加一个随机值，避免缓存雪崩
     * <p>长度为2 </p>
     *
     * @return 单位秒
     */
    int[] random() default {};
}
