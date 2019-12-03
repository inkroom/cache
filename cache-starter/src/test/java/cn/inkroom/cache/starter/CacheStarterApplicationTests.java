package cn.inkroom.cache.starter;

import cn.inkroom.cache.starter.service.TestServiceExample;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class CacheStarterApplicationTests {
    @Autowired
    private RedisTemplate template;
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private TestServiceExample bean;

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
    }

    @Test
    void testParam() {
        String name = "name";
        int age = 32;
        bean.param(name, age);

        Assertions.assertTrue(template.hasKey(name + "-" + age));

//        bean.out(name, age);
    }
}
