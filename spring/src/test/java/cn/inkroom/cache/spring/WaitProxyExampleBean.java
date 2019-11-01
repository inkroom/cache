package cn.inkroom.cache.spring;

import cn.inkroom.cache.core.annotation.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WaitProxyExampleBean {

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
}
