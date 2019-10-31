package cn.inkroom.cache.core.db;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author 墨盒
 * @date 2019/10/27
 */
public class RedisCacheTemplate implements CacheTemplate {

    private RedisTemplate<Object, Object> template;

    public RedisCacheTemplate(RedisTemplate<Object, Object> template) {
        this.template = template;
    }

    @Override
    public void set(String key, Object value, long ttl) throws Throwable {
        template.opsForValue().set(key, value, ttl, TimeUnit.MILLISECONDS);
    }

    @Override
    public Object get(String key) throws Throwable {
        return template.opsForValue().get(key);
    }

    @Override
    public long ttl(String key) throws Throwable {
        Long expire = template.getExpire(key);
        return expire == null ? -2 : expire;
    }

    @Override
    public boolean del(String key) throws Throwable {
        Boolean b = template.delete(key);
        return b == null ? false : b;
    }
}
