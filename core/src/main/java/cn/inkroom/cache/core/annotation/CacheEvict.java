package cn.inkroom.cache.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于删除
 *
 * @author 墨盒
 * @date 2019/10/27
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheEvict {
    /**
     * 要删除的key，ognl表达式
     *
     * @return
     */
    String key();


}
