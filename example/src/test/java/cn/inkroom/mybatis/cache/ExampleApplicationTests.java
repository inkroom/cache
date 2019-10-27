package cn.inkroom.mybatis.cache;

import cn.inkroom.mybatis.cache.dao.CacheDao;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;

@SpringBootTest
class ExampleApplicationTests {
    @Autowired
    private CacheDao dao;
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 测试对同一个key的上锁情况
     * @throws Exception
     */
    @Test
    void oneKey() throws Exception {
        int count = 10;
        CountDownLatch countDownLatch = new CountDownLatch(count);

        CountDownLatch all = new CountDownLatch(count);

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
    }
    /**
     * 测试对复数key的上锁情况
     * @throws Exception
     */
    @Test
    void mulKey() throws Exception {
        int count = 10;
        CountDownLatch countDownLatch = new CountDownLatch(count);

        CountDownLatch all = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            int finalI = i;
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


}
