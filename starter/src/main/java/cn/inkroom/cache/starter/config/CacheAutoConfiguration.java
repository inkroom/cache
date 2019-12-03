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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

/**
 * @author 墨盒
 * @date 2019/12/3
 */
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfiguration {


    @Bean
    public cn.inkroom.cache.core.db.CacheTemplate redisCacheTemplate(RedisTemplate<Object, Object> template) {
        return new cn.inkroom.cache.core.db.RedisCacheTemplate(template);
    }

    @Bean
    public cn.inkroom.cache.core.script.ScriptEngine scriptEngine() {
        return new cn.inkroom.cache.core.script.AviatorEngine();
    }

    @Bean
    public cn.inkroom.cache.core.plugins.StaticsPlugin staticsPlugin() {
        return new cn.inkroom.cache.core.plugins.StaticsPlugin() {
            @Override
            public void miss(String id, Cache cache, String key, Map<String, Object> args) {

            }

            @Override
            public void hit(String id, Cache cache, String key, Map<String, Object> args) {

            }

            @Override
            public void hitAgain(String id, Cache cache, String key, Map<String, Object> args) {

            }
        };
    }
    @Bean
    public cn.inkroom.cache.core.sync.SyncTool syncTool(){
        return new cn.inkroom.cache.core.sync.JdkSyncTool();
    }




    @Bean
    public CacheCore cacheCore(CacheTemplate cacheTemplate, CacheProperties properties,
                               @Autowired(required = false) ScriptEngine engine,
                               @Autowired(required = false) StaticsPlugin plugin,
                               @Autowired(required = false) SyncTool tool) {

        CacheCore core = new CacheCore();
        core.setCacheTemplate(cacheTemplate);
        core.setEngine(engine);
        core.setSyncTool(tool);

        if (properties.isSync()) {
            core.setSync(properties.isSync());
        }
        core.setPlugin(plugin);

        return core;
    }

    @Bean
    public CacheBeanPostProcessor cacheBeanPostProcessor(CacheCore cacheCore) {
        return new CacheBeanPostProcessor(cacheCore);
    }

}
