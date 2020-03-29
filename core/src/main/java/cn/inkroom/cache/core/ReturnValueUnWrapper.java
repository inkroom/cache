package cn.inkroom.cache.core;

/**
 * 对返回值进行一定的包装，目前用于mybatis插件
 */
public interface ReturnValueUnWrapper {

    Object wrapper(Object returnValue);
}
