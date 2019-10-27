package cn.inkroom.mybatis.cache.sync;

/**
 * 上锁工具
 *
 * @author 墨盒
 * @date 2019/10/26
 */
public interface SyncLock {
    /**
     * 上锁
     *
     * @param key redis缓存key
     * @return
     */
    boolean lock(String key);

    /**
     * 解锁
     *
     * @param key redis缓存key
     * @return
     */
    boolean unlock(String key);

}
