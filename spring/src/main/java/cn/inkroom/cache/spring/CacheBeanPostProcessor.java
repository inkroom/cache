package cn.inkroom.cache.spring;

import cn.inkroom.cache.core.CacheCore;
import cn.inkroom.cache.core.annotation.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CacheBeanPostProcessor implements BeanPostProcessor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private boolean cglib = true;//是否使用cglib，

    public CacheBeanPostProcessor() {
        logger.debug("创建");
    }

    private CacheCore core;

    public void setCglib(boolean cglib) {
        this.cglib = cglib;
    }

    public CacheBeanPostProcessor(CacheCore core) {
        this.core = core;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        Method[] methods = bean.getClass().getMethods();
        Map<String, Cache> cacheMap = new HashMap<>();
        Map<String, String[]> paramNames = new HashMap<>();

        boolean hasCache = false;
        Class beanClass = bean.getClass();
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        for (int i = 0; i < methods.length; i++) {
            Cache cache = methods[i].getAnnotation(Cache.class);
            if (cache != null) {
                hasCache = true;
                String[] parameterNames = u.getParameterNames(methods[i]);
//                logger.debug("p={}",Arrays.toString(parameterNames));
//                //获取参数名
//                Parameter[] parameters = methods[i].getParameters();
//                String[] names = new String[parameters.length];
//                for (int j = 0; j < parameters.length; j++) {
//                    logger.debug("pName={}",parameters[i].getName());
//                    Param param = parameters[j].getAnnotation(Param.class);
//                    if (param != null) names[j] = param.value();
//                    else names[j] = "param" + (j + 1);
//                }
//由于jdk 代理传递的参数method是接口的，cglib的是方法本身的，因此这里使用的key不能带有class
                String methodName = methods[i].toString();

                methodName = methodName.substring(methodName.lastIndexOf(".") + 1);

                paramNames.put(methodName, parameterNames);

            }
            cacheMap.put(methods[i].getName(), cache);

        }

        if (!hasCache) {//没有cache注解，不做代理
            return bean;
        }


        CacheInvocationHandler h = new CacheInvocationHandler(bean, core);

        h.setCacheMap(cacheMap);
        h.setParamName(paramNames);
        h.setClassName(beanClass.getName());

        logger.debug("interface={}", Arrays.toString(beanClass.getInterfaces()));

        if (!cglib) {//使用jdk代理
            Object value = Proxy.newProxyInstance(beanClass
                    .getClassLoader(), beanClass.getInterfaces(), h);

            logger.debug("proxy={}", value);

            return value;
        }

//        logger.debug("[cache] 创建代理对象");
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(bean.getClass());
        enhancer.setCallback(h);

        return enhancer.create();
    }

    private Cache getCache(Method method) {
        Cache cache = method.getAnnotation(Cache.class);
        return cache;

    }
}
