package cn.inkroom.cache.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 支持同时缓存多个结果，适用于VO或DTO对象
 * <br/>
 * cache的部分属性会被忽略，且可能不会出现cache的统计信息
 * <br/>
 * 使用该注解时请再使用@Cache对顶层对象做缓存
 * <p>
 * 如下：
 * \@Caches(value={\@Cache})
 * \@Cache()
 * public User find(){}
 *
 * @author 墨盒
 * @date 2019/12/20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Caches {

    Cache[] value();

    CacheEvict[] evict() default {};
}
