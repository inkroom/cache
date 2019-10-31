package cn.inkroom.cache.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WaitProxyExampleBean {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String toString() {
        logger.debug("toString");
        return super.toString();
    }
}
