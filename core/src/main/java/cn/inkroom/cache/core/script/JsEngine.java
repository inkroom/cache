package cn.inkroom.cache.core.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * js解析引擎，语法简单易学，但是性能比SpEl引擎差
 *
 * @author 墨盒
 * @date 2019/10/27
 */
public class JsEngine implements ScriptEngine {

    private javax.script.ScriptEngine engine;
    private SimpleBindings bindings;

    private Logger log = LoggerFactory.getLogger(getClass());

    public JsEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        bindings = new SimpleBindings();
        try {
            engine = manager.getEngineByName("javascript");
            engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public String express(String express, Map<String, Object> args) {
        args.forEach(new BiConsumer<String, Object>() {
            @Override
            public void accept(String s, Object o) {
                bindings.put(s, o);
            }
        });
        try {

            Object value = engine.eval(express);

            bindings.clear();

            if (value == null) return null;
            return value.toString();
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean booleanExpress(String express, Map<String, Object> args) {
        args.forEach(new BiConsumer<String, Object>() {
            @Override
            public void accept(String s, Object o) {
                bindings.put(s, o);
            }
        });
        try {

            Object value = engine.eval(express);
            bindings.clear();

            if (value == null) return false;
            return Boolean.parseBoolean(value.toString());
        } catch (ScriptException e) {
            e.printStackTrace();
            return false;
        }
    }
}
