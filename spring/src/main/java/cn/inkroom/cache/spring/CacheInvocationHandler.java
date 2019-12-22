package cn.inkroom.cache.spring;

import cn.inkroom.cache.core.CacheCore;
import cn.inkroom.cache.core.Task;
import cn.inkroom.cache.core.annotation.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheInvocationHandler implements InvocationHandler, MethodInterceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Object target;
    private CacheCore core;

    private Map<String, Cache> cacheMap;
    private Map<String, String[]> paramName;

    private String className;

    public CacheInvocationHandler(Object target, CacheCore core) {
        this.target = target;
        this.core = core;
        cacheMap = new HashMap<>();
    }

    public void setParamName(Map<String, String[]> paramName) {
        this.paramName = paramName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setCacheMap(Map<String, Cache> cacheMap) {
        this.cacheMap = cacheMap;
    }

    public void setCore(CacheCore core) {
        this.core = core;
    }

    public CacheInvocationHandler(Object target) {
        this.target = target;
        this.core = new CacheCore();
    }

    /**
     * jdk的方法拦截
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    @Deprecated
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Cache cache = cacheMap.get(method.getName());
        if (cache == null) return method.invoke(target, args);
        //拼接id
        String id = className + "." + method.getName();
        Map<String, Object> argsMap = getArgs(method, args);

        return core.query(method.toString().replaceAll("^[^ ]+ [^ ]+ ", ""), argsMap, () -> method.invoke(target, args), null);
    }

    /**
     * spring 的方法拦截
     *
     * @param o
     * @param method
     * @param objects
     * @param methodProxy
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        String id = method.toString().replaceAll("^[^ ]+ [^ ]+ ", "");

        Cache cache = cacheMap.get(id);

        if (cache == null) return methodProxy.invokeSuper(o, objects);
        //拼接id

        logger.debug("{}", method.getName());
        logger.debug("{}", method.toString());
        logger.debug("id={}", id);


        Map<String, Object> args = getArgs(method, objects);

        return core.query(id, args, () -> methodProxy.invokeSuper(o, objects), null);
    }


    private Map<String, Object> getArgs(Method method, Object[] args) {

        String methodName = method.toString();
        methodName = methodName.substring(methodName.lastIndexOf(".") + 1);

        Map<String, Object> map = new HashMap<>();
        String[] names = paramName.get(methodName);
        for (int i = 0; i < args.length; i++) {
            map.put(names[i], args[i]);
        }

        return map;
    }

}
