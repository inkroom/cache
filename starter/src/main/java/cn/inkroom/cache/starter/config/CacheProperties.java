package cn.inkroom.cache.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 墨盒
 * @date 2019/12/3
 */
@ConfigurationProperties(prefix = "cn.cache")
public class CacheProperties extends cn.inkroom.cache.core.config.CacheProperties {
}

