package cn.inkroom.mybatis.cache.annotation;

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
     *
     * @return
     */
    boolean sync() default false;


}
