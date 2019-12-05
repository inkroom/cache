package cn.inkroom.cache.starter;

import cn.inkroom.cache.starter.config.CacheProperties;
import cn.inkroom.cache.starter.service.TestServiceExample;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class CacheStarterApplicationTests {
    @Autowired
    @Qualifier(value = "redisTemplate")
    private RedisTemplate template;
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private TestServiceExample bean;
    @Autowired
    private CacheProperties properties;


    @BeforeEach
    void before() {
        template.execute((RedisCallback) redisConnection -> {
            redisConnection.flushAll();
            return true;
        });
    }

    @Test
    void test() {
        logger.debug("[数据={}]", bean.toString());
        Assertions.assertTrue(template.hasKey(properties.getKeyPrefix() + "toString"));
    }

    @Test
    void testParam() {
        String name = "name";
        int age = 32;
        bean.param(name, age);

        Assertions.assertTrue(template.hasKey(properties.getKeyPrefix() + name + "-" + age));

//        bean.out(name, age);
    }
}
