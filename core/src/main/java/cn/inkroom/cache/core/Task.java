package cn.inkroom.cache.core;

/**
 * 用于执行真正的获取数据的回调
 *
 * @author 墨盒
 * @date 2019/10/27
 */
public interface Task {
    /**
     * 执行方法
     * @return
     */
    Object proceed()throws Throwable;

}
