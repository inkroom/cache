## cache

基于Spring的缓存实现方案，可应用于SpringBoot、mybatis


### 优点

- 充分考虑高并发，避免高并发情况下请求打到db
- 尽量避免缓存雪崩和缓存穿透
- 允许自定义缓存命中情况统计
- 高度自定义，解析引擎、锁方案、缓存底层均可自由更换

### 项目结构

- core 核心实现
- example 在spring中的使用方法
- mybatis 基于mybatis的实现，可在单独mybatis环境中使用
- spring spring环境，基于动态代理
- starter SpringBoot starter，自动配置


### 开发

#### 环境

- redis
- mysql

#### 步骤

修改example/src/main/resources/application.properties 数据库配置信息

cd /
mvn package

### 使用方法

#### mybatis

#### spring

#### SpringBoot

---
更多使用方法见 example

#### 注册
使用spring注册CachePlugin即可

SpringBoot中如下

```java

 @Bean
    public CacheCore cacheCore(RedisTemplate redisTemplate) {
        CacheCore core = new CacheCore();
// 自定义缓存使用方案
        core.setCacheTemplate(new RedisCacheTemplate(redisTemplate));
    //自定义统计
        core.setPlugin(new StaticsPlugin() {});
//自定义脚本解析引擎
//        core.setEngine();
//自定义加锁方案
//        core.setSyncTool();
//设置全局同步配置，会被注解中的配置属性覆盖
//        core.setSync();
        return core;
    }

@Bean
public CachePlugin cachePlugin(CacheCore core) {
    CachePlugin plugin = new CachePlugin();
    plugin.setProperties(new Properties());
    plugin.setCore(core);
    return plugin;
}

```

#### 注解

在dao层中使用`@Cache`注解

