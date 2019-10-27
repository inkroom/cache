package cn.inkroom.mybatis.cache;

import cn.inkroom.mybatis.cache.dao.CacheDao;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExampleApplicationTests {
    @Autowired
    private CacheDao dao;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void contextLoads() throws Exception{
        logger.debug(" list = {}", dao.list(2, 3));
    }

}
