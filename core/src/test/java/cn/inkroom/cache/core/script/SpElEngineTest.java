package cn.inkroom.cache.core.script;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 墨盒
 * @date 2019/10/27
 */
class SpElEngineTest {
    private Logger log = LoggerFactory.getLogger(getClass());

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

    @Test
    void booleanExpress() throws Exception {
        ScriptEngine engine = new SpElEngine();
        Map<String, Object> map = new HashMap<>();
        map.put("page", 42);
        map.put("size", "name");
        map.put("count", new ArrayList<>(Arrays.asList(32, 543, 63, 3)));

        Map<String, String> temp = new HashMap<>();
        temp.put("page", "230d");

        map.put("temp", temp);


        assertTrue(engine.booleanExpress("#page==42", map));
        assertFalse(engine.booleanExpress("#page!=42", map));

        assertTrue(engine.booleanExpress("#size=='name'", map));
        assertFalse(engine.booleanExpress("#size!='name'", map));

        assertTrue(engine.booleanExpress("#page==42 && #size=='name'", map));
        assertFalse(engine.booleanExpress("#page!=42 || #size!='name'", map));
        assertTrue(engine.booleanExpress("#page==42 || #size!='name'", map));
        assertTrue(engine.booleanExpress("#page!=42 || #size=='name'", map));

        //测试list

        log.debug("size={}", engine.express("#count.size()", map));
        assertTrue(engine.booleanExpress("#count.size() == 4", map));

        assertFalse(engine.booleanExpress("#count.size() != 4", map));

        assertEquals("32", engine.express("#count[0]", map));

        assertEquals("230d", engine.express("#temp.page", map));
        assertEquals("230d", engine.express("#temp['page']", map));
    }
    /**
     * 测试效率
     *
     * @throws Exception
     */
    @Test
    public void testPerformance() throws Exception {


        long start = System.currentTimeMillis();
        int count = 100;
        for (int i = 0; i < count; i++) {
            booleanExpress();
        }
        log.debug("SpEl执行{}次，耗时{}", count, (System.currentTimeMillis() - start));
    }
}