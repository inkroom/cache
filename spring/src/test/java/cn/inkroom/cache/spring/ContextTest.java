package cn.inkroom.cache.spring;

import org.junit.jupiter.api.Test;


import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:spring.xml")
class ContextTest {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private WaitProxyExampleBean bean;

    @Test
    void test() {
        logger.debug("[数据={}]", bean.toString());
    }

}
