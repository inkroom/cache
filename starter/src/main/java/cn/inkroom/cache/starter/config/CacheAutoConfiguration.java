package cn.inkroom.cache.starter.config;

import cn.inkroom.cache.core.CacheCore;
import cn.inkroom.cache.core.annotation.Cache;
import cn.inkroom.cache.core.db.CacheTemplate;
import cn.inkroom.cache.core.db.RedisCacheTemplate;
import cn.inkroom.cache.core.plugins.StaticsPlugin;
import cn.inkroom.cache.core.script.AviatorEngine;
import cn.inkroom.cache.core.script.ScriptEngine;
import cn.inkroom.cache.core.sync.JdkSyncTool;
import cn.inkroom.cache.core.sync.SyncTool;
import cn.inkroom.cache.spring.CacheBeanPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author 墨盒
 * @date 2019/12/3
 */
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfiguration {
    @Autowired
    @Qualifier("redisTemplate")
//            @Resource(name = "redisTemplate")
            RedisTemplate<Object, Object> template;

    @Bean
    @ConditionalOnMissingBean(CacheTemplate.class)
    public cn.inkroom.cache.core.db.CacheTemplate redisCacheTemplate() {
        return new cn.inkroom.cache.core.db.RedisCacheTemplate(template);
    }

    @Bean
    @ConditionalOnMissingBean(SyncTool.class)
    public SyncTool syncTool() {
        return new JdkSyncTool();
    }

    @Bean
    public CacheCore cacheCore(CacheTemplate cacheTemplate, CacheProperties properties,
                               @Autowired(required = false) ScriptEngine engine,
                               @Autowired(required = false) StaticsPlugin plugin,
                               SyncTool tool) {

        CacheCore core = new CacheCore();
        core.setCacheTemplate(cacheTemplate);
        if (engine != null)
            core.setEngine(engine);
        if (tool != null)
            core.setSyncTool(tool);

        core.setProperties(properties);
        core.setPlugin(plugin);
        return core;
    }

    @Bean
    public CacheBeanPostProcessor cacheBeanPostProcessor(CacheCore cacheCore) {
        return new CacheBeanPostProcessor(cacheCore);
    }

}
