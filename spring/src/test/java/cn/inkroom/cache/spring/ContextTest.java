package cn.inkroom.cache.spring;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:spring.xml")
@SuppressWarnings("all")
class ContextTest {
    @Autowired
    private RedisTemplate template;
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private WaitProxyExampleBean bean;

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
    }

}
