package cn.inkroom.cache.core.config;


/**
 * @author 墨盒
 * @date 2019/12/3
 */
public class CacheProperties {

    /**
     * 是否开启同步锁，
     */
    private boolean sync = false;
    /**
     * 缓存key前缀
     */
    private String keyPrefix = "";

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }


    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }
}

