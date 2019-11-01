package cn.inkroom.cache.spring;

import cn.inkroom.cache.core.CacheCore;
import cn.inkroom.cache.core.annotation.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CacheBeanPostProcessor implements BeanPostProcessor {

    private Logger logger = LoggerFactory.getLogger(getClass());


    public CacheBeanPostProcessor() {
        logger.debug("创建");
    }

    private CacheCore core;

    public CacheBeanPostProcessor(CacheCore core) {
        this.core = core;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        Method[] methods = bean.getClass().getMethods();
        Map<String, Cache> cacheMap = new HashMap<>();
        Map<String, String[]> paramNames = new HashMap<>();

        boolean hasCache = false;


        for (int i = 0; i < methods.length; i++) {
            Cache cache = methods[i].getAnnotation(Cache.class);
            if (cache != null) {
                hasCache = true;
                //获取参数名
                Parameter[] parameters = methods[i].getParameters();
                String[] names = new String[parameters.length];
                for (int j = 0; j < parameters.length; j++) {
                    Param param = parameters[j].getAnnotation(Param.class);
                    if (param != null) names[j] = param.value();
                    else names[j] = "param" + (j + 1);
                }

                paramNames.put(methods[i].toString(), names);

            }
            cacheMap.put(methods[i].getName(), cache);

        }

        if (!hasCache) {//没有cache注解，不做代理
            return bean;
        }

        CacheInvocationHandler h = new CacheInvocationHandler(bean, core);

        h.setCacheMap(cacheMap);
        h.setParamName(paramNames);

        logger.debug("[cache] 创建代理对象");
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(bean.getClass());
        enhancer.setCallback(h);

        return enhancer.create();

//        return Proxy.newProxyInstance(bean.getClass().getClassLoader(), bean.getClass().getClasses(),new CacheInvocationHandler(bean));
    }

    private Cache getCache(Method method) {
        Cache cache = method.getAnnotation(Cache.class);
        return cache;

    }
}
