package cn.inkroom.cache.spring;

import cn.inkroom.cache.core.CacheCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;

import java.lang.reflect.Proxy;

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
        CacheInvocationHandler h = new CacheInvocationHandler(bean, core);

        logger.debug("创建代理对象");
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(bean.getClass());
        enhancer.setCallback(h);

        return enhancer.create();

//        return Proxy.newProxyInstance(bean.getClass().getClassLoader(), bean.getClass().getClasses(),new CacheInvocationHandler(bean));
    }
}
