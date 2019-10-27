## mybatis-cache

基于mybatis和Spring的缓存实现方案


### 优点

- 充分考虑高并发，避免高并发情况下请求打到db
- 尽量避免缓存雪崩和缓存穿透

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
public CachePlugin cachePlugin() {
    CachePlugin plugin = new CachePlugin();
    plugin.setProperties(new Properties());
    return plugin;
}

```

#### 注解

在dao层中使用`@Cache`注解


#### 替换缓存实现方案

在Spring中注册`name=cacheTemplate`的`SyncLock`实现类即可

或手动调用`CachePlugin.setCacheTemplate`亦可

#### 替换锁实现方案