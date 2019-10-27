package cn.inkroom.mybatis.cache.ognl;

import org.apache.ibatis.ognl.MemberAccess;
import org.apache.ibatis.ognl.OgnlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Member;
import java.util.Map;

/**
 * @author 墨盒
 * @date 2019/10/27
 */
public class CacheOgnlContext {
    private static Logger logger = LoggerFactory.getLogger(CacheOgnlContext.class);

    public static OgnlContext getContext() {
        return new OgnlContext(null, null, new MemberAccess() {
            @Override
            public Object setup(Map context, Object target, Member member, String propertyName) {
                logger.debug("context-{},target={},member={},propertyName={}", context, target, member, propertyName);
                return null;
            }

            @Override
            public void restore(Map context, Object target, Member member, String propertyName, Object state) {
                logger.debug("context-{},target={},member={},propertyName={},state={}", context, target, member, propertyName, state);
            }

            @Override
            public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
                return true;
            }
        });
    }
}
