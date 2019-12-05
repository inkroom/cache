package cn.inkroom.cache.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class CacheStarterApplication {
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate
                template = new RedisTemplate();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new RedisSerializer<Object>() {
            @Override
            public byte[] serialize(Object o) throws SerializationException {
                if (o == null) {
                    return null;
                }
                return o.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public Object deserialize(byte[] bytes) throws SerializationException {
                if (bytes.length == 0) {
                    return null;
                }
                return new String(bytes, StandardCharsets.UTF_8);
            }
        });

        template.setValueSerializer(new JdkSerializationRedisSerializer());

        return template;


    }

    public static void main(String[] args) {
        SpringApplication.run(CacheStarterApplication.class, args);
    }

}
