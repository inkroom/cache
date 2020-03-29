package cn.inkroom.cache.core.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 删除缓存注解
 *
 * @author 墨盒
 * @date 2020/3/29
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheEvict {
    /**
     * 要删除的key
     *
     * @return
     */
    @AliasFor("value")
    String key();

    @AliasFor("key")
    String value();


    /**
     * 是否执行该条删除
     *
     * @return
     */
    String condition() default "";


}
