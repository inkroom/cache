package cn.inkroom.cache.spring;

import cn.inkroom.cache.core.annotation.Cache;
import cn.inkroom.cache.core.annotation.Caches;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Caches(
            {@Cache(key = "'vo.data='+rv.name",data = "rv.name"), @Cache(key = "'vo.age='+rv.age",data = "rv.age")}
    )
    @Cache(key = "'vo-'+name+'-'+age")
    @Override
    public VO vo(String name, int age) {

        VO vo = new VO();
        vo.setName(name);
        vo.setAge(age);
        return vo;
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
