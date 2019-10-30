package cn.inkroom.cache.core.script;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AviatorEngine implements ScriptEngine {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String express(String express, Map<String, Object> args) {
        Expression expression = AviatorEvaluator.compile(express);
        Object execute = expression.execute(args);
        if (execute == null) return null;
        return execute.toString();
    }

    @Override
    public boolean booleanExpress(String express, Map<String, Object> args) {
        Expression expression = AviatorEvaluator.compile(express);
        Object execute = expression.execute(args);
        if (execute == null) return false;
        return Boolean.parseBoolean(execute.toString());
    }
}
