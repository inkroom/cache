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

使用以下配置方案

```xml
    <!--    一些配置项-->
    <bean id="cacheProperties" class="cn.inkroom.cache.core.config.CacheProperties">
        <property name="keyPrefix" value=""/>
        <property name="sync" value="true"/>
    </bean>
    <bean class="cn.inkroom.cache.spring.CacheBeanPostProcessor">
        <constructor-arg ref="cacheCore"/>
    </bean>

    <bean id="cacheCore" class="cn.inkroom.cache.core.CacheCore">
        <constructor-arg name="cacheTemplate">
            <bean class="cn.inkroom.cache.core.db.RedisCacheTemplate">
                <constructor-arg ref="redisTemplate"/>
            </bean>
        </constructor-arg>
        <property name="properties" ref="cacheProperties"/>
        <property name="syncTool">
            <bean class="cn.inkroom.cache.core.sync.JdkSyncTool"/>
        </property>
    </bean>

    <bean id="configuration" class="org.springframework.data.redis.connection.RedisStandaloneConfiguration">
        <constructor-arg name="hostName" value="127.0.0.1"/>
        <constructor-arg name="port" value="6379"/>
    </bean>

    <!-- Spring-redis连接池管理工厂 -->
    <bean id="jedisConnectionFactory" class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory">
        <constructor-arg ref="configuration"/>
    </bean>

    <!-- redis template definition -->
    <bean id="redisTemplate" class="org.springframework.data.redis.core.RedisTemplate">
        <property name="connectionFactory" ref="jedisConnectionFactory"/>
        <property name="keySerializer">
            <bean class="org.springframework.data.redis.serializer.StringRedisSerializer"/>
        </property>
        <property name="valueSerializer">
            <bean class="org.springframework.data.redis.serializer.JdkSerializationRedisSerializer"/>
        </property>
        <property name="hashKeySerializer">
            <bean class="org.springframework.data.redis.serializer.StringRedisSerializer"/>
        </property>
        <property name="hashValueSerializer">
            <bean class="org.springframework.data.redis.serializer.JdkSerializationRedisSerializer"/>
        </property>
        <!--开启事务  -->
    </bean>
<!--    根据该接口自行实现，注入到core中即可-->
<!--    <bean class="cn.inkroom.cache.core.plugins.StaticsPlugin" />-->

```


#### SpringBoot

可注入ScriptEngine、SyncTool、CacheTemplate、StaticsPlugin

样例
```java
   @Bean
    public cn.inkroom.cache.core.db.CacheTemplate redisCacheTemplate(RedisTemplate<Object, Object> template) {
        //默认使用redis做缓存引擎    
        return new cn.inkroom.cache.core.db.RedisCacheTemplate(template);
    }

    @Bean
    public cn.inkroom.cache.core.script.ScriptEngine scriptEngine() {                                                                     
        //默认使用该引擎，不建议修改
        return new cn.inkroom.cache.core.script.AviatorEngine();
    }

    @Bean
    public cn.inkroom.cache.core.plugins.StaticsPlugin staticsPlugin() {
        // 请自行根据业务需求实现
        return new cn.inkroom.cache.core.plugins.StaticsPlugin() {
            @Override
            public void miss(String id, Cache cache, String key, Map<String, Object> args,boolean first) {
                
            }

            @Override
            public void hit(String id, Cache cache, String key, Map<String, Object> args,boolean first) {

            }

        };
    }
    @Bean
    public cn.inkroom.cache.core.sync.SyncTool syncTool(){
        // 默认使用该加锁方案，可自行替换
        return new cn.inkroom.cache.core.sync.JdkSyncTool();
    }

```

配置项

```properties
#  启用同步锁，可尽可能减少数据库访问次数
cn.cache.sync=true
# 统一加在缓存key的前缀
cn.cache.key-prefix=prefix
```


---
更多使用方法见 example或测试用例

#### 注解

在dao层中使用`@Cache`注解

