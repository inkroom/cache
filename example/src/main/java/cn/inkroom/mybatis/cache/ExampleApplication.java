package cn.inkroom.mybatis.cache;

import cn.inkroom.mybatis.cache.ser.JsonSerializer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Properties;

@SpringBootApplication
@MapperScan("cn.inkroom.mybatis.cache.dao")
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @Bean
    public CachePlugin cachePlugin() {
        CachePlugin plugin = new CachePlugin();
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
