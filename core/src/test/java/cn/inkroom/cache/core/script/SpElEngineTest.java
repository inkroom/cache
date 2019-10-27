package cn.inkroom.cache.core.script;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 墨盒
 * @date 2019/10/27
 */
class SpElEngineTest {

    @Test
    void express() {
        ScriptEngine engine = new SpElEngine();


        Map<String, Object> map = new HashMap<String, Object>();

        map.put("page", 42);
        map.put("size", "name");


        assertEquals(map.get("page").toString(), engine.express("#page.toString()", map));

        assertEquals(map.get("page").toString(), engine.express("['page']", map));

        assertEquals(map.get("page") + "+" + map.get("size"), engine.express("#page + '+'+ #size", map));

    }
}