package cn.inkroom.cache.core.script;

import java.util.Map;

/**
 * 脚本引擎，主要用于解析 key 等注解值
 *
 * @author 墨盒
 * @date 2019/10/27
 */
public interface ScriptEngine {

    /**
     * 解析值
     *
     * @param express express表达式
     * @param args    参数
     * @return
     */
    String express(String express, Map<String, Object> args);

}
