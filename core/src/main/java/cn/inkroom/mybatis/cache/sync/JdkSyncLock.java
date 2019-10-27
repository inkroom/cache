package cn.inkroom.mybatis.cache.sync;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于jdk 实现的lock类
 *
 * @author 墨盒
 * @date 2019/10/26
 */
public class JdkSyncLock implements SyncLock {

    private Map<String, Lock> lockMap = new HashMap<>();

    @Override
    public boolean lock(String key) {
        // TODO: 2019/10/26 这里好像会有线程安全问题
        if (lockMap.get(key) == null) {
            ReentrantLock lock = new ReentrantLock();
            lockMap.put(key, lock);
            lock.lock();
        } else {
            lockMap.get(key).lock();
        }
        return true;
    }

    @Override
    public boolean unlock(String key) {
        if (!lockMap.containsKey(key)) {
            return false;
        } else {
            lockMap.get(key).unlock();
        }
        return true;
    }
}
