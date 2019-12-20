package cn.inkroom.cache.spring;

import cn.inkroom.cache.core.annotation.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

public class WaitProxyExampleBean implements ExampleInterface {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Cache(key = "'toString'")
    @Override
    public String toString() {
        logger.debug("toString");
        return super.toString();
    }

    @Cache(key = "name+'-'+age")
    public boolean param(String name, int age) {

        return true;
    }

    @Override
    @Cache(key = "name+'-'")
    public boolean param(String name) {
        return false;
    }

    /**
     * 用于测试interface没有的方法
     *
     * @param name
     * @param age
     * @return
     */
    @Cache(key = "name+'='+age")
    public boolean out(String name, int age) {

        return true;
    }

}
