package cn.inkroom.cache.mybatis;

import cn.inkroom.cache.core.ReturnValueWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class MybatisReturnValueWrapper implements ReturnValueWrapper {

    private Logger logger = LoggerFactory.getLogger(getClass());


    private Class type;

    public MybatisReturnValueWrapper(Class type) {
        this.type = type;
    }

    @Override
    public Object wrapper(Object value) {
        if (value instanceof ArrayList) {
            ArrayList list = (ArrayList) value;

            if (this.type.isInstance(value)) {//本身查询list
                return value;
            }
            //查询单个，但是个数为0，代表为null
            if (list.size() == 0) {
                return null;
            }
//            if (type.isInstance(list.get(0))) {
            //查询单个bean；可能出现查询单个bean，但是查出多个结果，此时会缓存一个值，然后mybatis去报错
            return list.get(0);
//            }
        }
        return value;

    }
}
