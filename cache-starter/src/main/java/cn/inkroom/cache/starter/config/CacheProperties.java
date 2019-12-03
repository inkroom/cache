package cn.inkroom.cache.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 墨盒
 * @date 2019/12/3
 */
@ConfigurationProperties(prefix = "cn.cache")
public class CacheProperties {

    /**
     * 是否开启同步锁，
     */
    private boolean sync;
    /**
     * 实际使用的syncTool在容器中的name
     */
    private String syncToolName;

    public String getSyncToolName() {
        return syncToolName;
    }

    public void setSyncToolName(String syncToolName) {
        this.syncToolName = syncToolName;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }
}

