package cn.inkroom.cache.core.util;

import cn.inkroom.cache.core.annotation.Cache;
import cn.inkroom.cache.core.annotation.Caches;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 负责从id中获取注解
 *
 * @author 墨盒
 * @date 2019/12/21
 */
public class ReflectHelper {

    private Map<String, Cache> methodCacheMap = new HashMap<>();
    private Map<String, Caches> cachesMap = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Pattern idPattern = Pattern.compile("\\((.+)\\)$");

    private Pattern classNamePattern = Pattern.compile("^(.+)\\.(\\w+)\\((.*)\\)$");

    private Class convert(String className) throws Throwable {

        switch (className) {
            case "int":
                return Integer.TYPE;
            case "short":
                return Short.TYPE;
            case "long":
                return Long.TYPE;
            case "double":
                return Double.TYPE;
            case "float":
                return Float.TYPE;
            case "byte":
                return Byte.TYPE;
            case "char":
                return Character.TYPE;
            default:
                return Class.forName(className);
        }

    }

    /**
     * @param id 如cn.inkroom.Con.test(java.lang.String,int)
     * @return
     * @throws Throwable
     */
    private Method getMethod(String id) throws Throwable {
        //id 是方法全路径，不包括参数
        Method method = null;

        Matcher matcher = classNamePattern.matcher(id);

        if (matcher.groupCount() != 3 || !matcher.find()) {
            throw new IllegalArgumentException("不合法的id " + id);
        }


        String className = matcher.group(1);
        String methodName = matcher.group(2);
        String methodParams = matcher.group(3);
        if (methodParams.length() > 0) {
            String[] ps = methodParams.split(",");
            Class[] cs = new Class[ps.length];

            for (int i = 0; i < ps.length; i++) {
                cs[i] = convert(ps[i]);
            }
            method = Class.forName(className).getMethod(methodName, cs);
        } else {
            //没有参数
            method = Class.forName(className).getMethod(methodName);
        }
        return method;
    }

    /**
     * 获取Cache注解和返回值类型
     *
     * @param id 方法全路径，如cn.inkroom.cache.core.CacheCore.getCache(String)
     * @return cache
     * @throws Throwable
     */
    @SuppressWarnings("all")
    public Cache getCache(String id) throws Throwable {

        Cache c = methodCacheMap.get(id);
        if (c != null)
            return c;

        c = getMethod(id).getAnnotation(Cache.class);
        methodCacheMap.put(id, c);
        return c;
    }

    public Caches getCaches(String id) throws Throwable {
        Caches c = cachesMap.get(id);
        if (c != null)
            return c;

        c = getMethod(id).getAnnotation(Caches.class);
        cachesMap.put(id, c);
        return c;
    }


}
