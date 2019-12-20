package cn.inkroom.cache.example;

import cn.inkroom.cache.example.bean.Cache;
import cn.inkroom.cache.example.dao.CacheDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
@Transactional
@Rollback
@SuppressWarnings("all")
class ExampleApplicationTests {
    @Autowired
    private CacheDao dao;
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource(name = "redisTemplate")
    private RedisTemplate template;

    @BeforeEach
    void before() {
        template.execute((RedisCallback) redisConnection -> {
            redisConnection.flushAll();
            return true;
        });
    }

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
                        logger.debug("list={}", dao.list(2, 4));
                        Assertions.assertTrue(template.hasKey("2"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        all.countDown();
                    }
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
                        logger.debug("list={}", dao.list(finalI, 4));
                        Assertions.assertTrue(template.hasKey(finalI));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        all.countDown();
                    }
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

        Assertions.assertTrue(template.hasKey("" + key));

        Cache cache = new Cache();
        cache.setId(33223);
        cache.setAge(key);
        cache.setName("32233d但是");
        cache.setType(4);

        dao.insert(cache);

        Assertions.assertFalse(template.hasKey("" + key));
    }

    /**
     * 测试缓存null
     */
    @Test
    void testNull() {


        //null要缓存

        //查询单个，但是数据为null
        int key = new Random().nextInt(3333);
        Assertions.assertNull(dao.detail(key));
        Assertions.assertTrue(template.hasKey(key + ""));
        //测试结果是不是null
        Assertions.assertNull(template.opsForValue().get(key + ""));

        template.delete(key + "");
        Assertions.assertFalse(template.hasKey(key + ""));

        //查询集合，但是没有数据
        List<Cache> caches = dao.listById(key);
        Assertions.assertEquals(0, caches.size());
        Assertions.assertTrue(template.hasKey(key + ""));
        Assertions.assertEquals(caches, template.opsForValue().get(key + ""));


        //查询集合，有多条数据

        template.delete(key + "");
        Assertions.assertFalse(template.hasKey(key + ""));

        caches = dao.list(key, 3);
        Assertions.assertTrue(template.hasKey(String.valueOf(key)));
        Assertions.assertEquals(caches, template.opsForValue().get(key + ""));

        //查询集合，但是只有一条数据
        template.delete(key + "");
        Assertions.assertNotEquals(0, caches.size());
        List<Cache> newCaches = dao.listById(caches.get(0).getId());

        Assertions.assertTrue(template.hasKey(caches.get(0).getId() + ""));
        Assertions.assertEquals(newCaches, template.opsForValue().get(caches.get(0).getId() + ""));

        template.delete(caches.get(0).getId() + "");

        //查询单个，且有且只有一条数据
        Cache cache = dao.detail(caches.get(0).getId());

        Assertions.assertNotNull(cache);
        Assertions.assertTrue(template.hasKey(cache.getId() + ""));

        Assertions.assertEquals(cache, template.opsForValue().get(cache.getId() + ""));

        //查询单个，且有多条数据
        try {
            dao.detailNotOnlyOne(cache.getId());
            Assertions.fail("应该抛出有多个数据无法填充的异常");
        } catch (Exception e) {
            Assertions.assertTrue(template.hasKey(cache.getId() + ""));
        }


    }
}
