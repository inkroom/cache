package cn.inkroom.cache.example;

import cn.inkroom.cache.core.CacheCore;
import cn.inkroom.cache.core.annotation.Cache;
import cn.inkroom.cache.core.db.RedisCacheTemplate;
import cn.inkroom.cache.core.plugins.StaticsPlugin;
import cn.inkroom.cache.example.ser.JsonSerializer;
import cn.inkroom.cache.mybatis.CachePlugin;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Map;
import java.util.Properties;

@SpringBootApplication
@MapperScan("cn.inkroom.cache.example.dao")
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @Bean
    public CacheCore cacheCore(RedisTemplate redisTemplate) {
        CacheCore core = new CacheCore();
        core.setCacheTemplate(new RedisCacheTemplate(redisTemplate));
        core.setPlugin(new StaticsPlugin() {
            private Logger log = LoggerFactory.getLogger(getClass());

            @Override
            public void miss(String id, Cache cache, String key, Map<String, Object> args, boolean first) {
                log.debug("缓存穿透，key={}，方法={}", key, id);
            }

            @Override
            public void hit(String id, Cache cache, String key, Map<String, Object> args, boolean first) {
                log.debug("缓存命中，key={}，方法={}", key, id);
            }
        });
        return core;
    }

    @Bean
    public CachePlugin cachePlugin(CacheCore core) {
        CachePlugin plugin = new CachePlugin();
        plugin.setCore(core);
        plugin.setProperties(new Properties());
        return plugin;
    }

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory factory) {
        JsonSerializer serializer = new JsonSerializer();

        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.setConnectionFactory(factory);

        return template;
    }
}
