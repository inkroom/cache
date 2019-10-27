## mybatis-cache

基于mybatis和Spring的缓存实现方案

### 项目结构

- core 核心实现
- example 在spring中的使用方法


### 使用方法

#### 引入



#### 注册
使用spring注册CachePlugin即可

SpringBoot中如下

```java

@Bean
public CachePlugin cachePlugin() {
    CachePlugin plugin = new CachePlugin();
    plugin.setProperties(new Properties());
    return plugin;
}

```

#### 注解

在dao层中使用`@Cache`注解


#### 替换缓存实现方案

在Spring中注册`name=cacheTemplate`的`cn.inkroom.mybatis.cache.sync.SyncLock`实现类即可

或手动调用`CachePlugin.setCacheTemplate`亦可

#### 替换锁实现方案