package cn.inkroom.cache.core.script;

import java.util.Map;

/**
 * 脚本引擎，主要用于解析 key 等注解值
 * <p>默认使用Aviadtor</p>
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

    /**
     * 执行脚本，获取boolean类型返回值
     *
     * @param express
     * @param args
     * @return
     */
    boolean booleanExpress(String express, Map<String, Object> args);

    /**
     * 获取一个Object，用于指定缓存的对象
     *
     * @param express
     * @param args
     * @return
     */
    default Object objectExpress(String express, Map<String, Object> args) {
        return null;
    }
}
