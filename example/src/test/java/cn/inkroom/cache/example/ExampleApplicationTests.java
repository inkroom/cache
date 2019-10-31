package cn.inkroom.cache.example;

import cn.inkroom.cache.example.bean.Cache;
import cn.inkroom.cache.example.dao.CacheDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
@Transactional
@Rollback
class ExampleApplicationTests {
    @Autowired
    private CacheDao dao;
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource(name = "redisTemplate")
    private RedisTemplate template;

    /**
     * 测试对同一个key的上锁情况
     *
     * @throws Exception
     */
    @Test
    void oneKey() throws Exception {
        int count = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(count);

        final CountDownLatch all = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    logger.debug("list={}", dao.list(2, 4));
                    all.countDown();
                }
            }).start();
            countDownLatch.countDown();
        }
        all.await();
        logger.debug("list={}", dao.list(2, 4));
    }

    /**
     * 测试对复数key的上锁情况
     *
     * @throws Exception
     */
    @Test
    void mulKey() throws Exception {
        int count = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(count);

        final CountDownLatch all = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    logger.debug("list={}", dao.list(finalI, 4));
                    all.countDown();
                }
            }).start();
            countDownLatch.countDown();
        }
        all.await();
    }

    /**
     * 测试Condition效果
     *
     * @throws Exception
     */
    @Test
    void testCondition() throws Exception {

        //不缓存
        dao.condition(32, 4);
        Assertions.assertFalse(template.hasKey("page_con_" + 32));
        //缓存
        dao.condition(2, 4);
        Assertions.assertTrue(template.hasKey("page_con_" + 2));
//测试根据结果判断
        //缓存
        dao.conditionRv(32, 4);
        Assertions.assertTrue(template.hasKey("page_conRv_" + 32));
        //不缓存
        dao.conditionRv(2, 4);
        Assertions.assertFalse(template.hasKey("page_conRv_" + 2));

    }

    /**
     * 测试删除功能
     */
    @Test
    void testInsert() {

        //创建一个缓存
        int key = new Random().nextInt();
        dao.list(key, 4);

        Assertions.assertTrue(template.hasKey("page_" + key));

        Cache cache = new Cache();
        cache.setId(33223);
        cache.setAge(key);
        cache.setName("32233d但是");
        cache.setType(4);

        dao.insert(cache);

        Assertions.assertFalse(template.hasKey("page_" + key));
    }
}
