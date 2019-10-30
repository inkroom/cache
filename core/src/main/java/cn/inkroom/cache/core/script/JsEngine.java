package cn.inkroom.cache.core.script;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author 墨盒
 * @date 2019/10/27
 */
public class JsEngine implements ScriptEngine {

    private javax.script.ScriptEngine engine;

    public JsEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        try {
            engine = manager.getEngineByName("javascript");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public String express(String express, Map<String, Object> args) {
        args.forEach(new BiConsumer<String, Object>() {
            @Override
            public void accept(String s, Object o) {
                engine.put(s, o);
            }
        });
        try {

            Object value = engine.eval(express);
            if (value == null) return null;
            return value.toString();
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean booleanExpress(String express, Map<String, Object> args) {
        return false;
    }
}
