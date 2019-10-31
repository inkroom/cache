package cn.inkroom.cache.spring;

import cn.inkroom.cache.core.CacheCore;
import cn.inkroom.cache.core.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CacheInvocationHandler implements InvocationHandler, MethodInterceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Object target;
    private CacheCore core;

    public CacheInvocationHandler(Object target, CacheCore core) {
        this.target = target;
        this.core = core;
    }

    public void setCore(CacheCore core) {
        this.core = core;
    }

    public CacheInvocationHandler(Object target) {
        this.target = target;
        this.core =new CacheCore();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.debug("代理对象");
        return null;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        logger.debug("执行cglib");
        //拼接id
        String id = o.getClass().getName() + "." + method.getName();
        Map<String, Object> args = getArgs(objects);

        return core.query(id, args, () -> method.invoke(o, objects), null);
    }


    private Map<String, Object> getArgs(Object[] args) {

        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            map.put("param" + (i + 1), args[i]);
        }

        return map;
    }

}
